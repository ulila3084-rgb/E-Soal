package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.QuestionEntity
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

object MasterSoalExporter {

    fun toArabicNumber(number: Int): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val str = number.toString()
        val sb = StringBuilder()
        for (ch in str) {
            if (ch in '0'..'9') {
                sb.append(arabicDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toArabicJilid(jilidName: String): String {
        val digits = jilidName.filter { it.isDigit() }
        return if (digits.isNotEmpty()) {
            val num = digits.toIntOrNull() ?: 1
            "جلد ${toArabicNumber(num)}"
        } else {
            "جلد $jilidName"
        }
    }

    data class ExamMetadata(
        val jilid: String = "Jilid 1",
        val namaMadrasah: String = "المعهد الإسلامي / مدرسة تعليم اللغة العربية والعلوم الإسلامية",
        val judulUjian: String = "امتحان الفصل الدراسي / بنك الأسئلة الموحد",
        val mataPelajaran: String = "النحو والصرف والقراءة",
        val malamUjian: String = "ليلة الأحد",
        val tanggalHijriyah: String = "١٥ ربيع الأول ١٤٤٨ هـ",
        val durasiWaktu: String = "٩٠ دقيقة",
        val modeSoal: String = "campur" // "campur" or "pilihan"
    )

    /**
     * Generates a complete F4 (Folio 215x330mm) HTML Master Soal document
     * with RTL Arabic typography, borders, Kop, and sections.
     */
    fun generateMasterSoalHtml(
        metadata: ExamMetadata,
        questions: List<QuestionEntity>
    ): String {
        val jilidDigits = metadata.jilid.filter { it.isDigit() }
        val jilidNum = jilidDigits.toIntOrNull() ?: 1
        val arabicJilid = "جلد ${toArabicNumber(jilidNum)}"
        val latinJilid = "JILID $jilidNum"

        val sb = StringBuilder()
        sb.append("""
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
<meta charset="UTF-8">
<title>Master Soal $latinJilid</title>
<style>
  @page {
    size: 215mm 330mm; /* Standar Ukuran Kertas F4 / Folio */
    margin: 12mm 15mm 15mm 15mm;
  }
  * {
    box-sizing: border-box;
  }
  body {
    font-family: 'Traditional Arabic', 'Amiri', 'Scheherazade', 'Arial', serif;
    direction: rtl;
    text-align: right;
    margin: 0;
    padding: 0;
    font-size: 16pt;
    line-height: 1.6;
    color: #000000;
    background-color: #ffffff;
  }
  .page-container {
    width: 100%;
    max-width: 210mm;
    margin: 0 auto;
    padding: 5mm;
  }
  
  /* Kop / Header Ujian */
  .header-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 8px;
  }
  .header-table td {
    padding: 2px 4px;
    vertical-align: middle;
  }
  .corner-box {
    border: 2px solid #000;
    padding: 6px 12px;
    text-align: center;
    border-radius: 6px;
    background-color: #f9f9f9;
  }
  .corner-box .latin-jilid {
    font-family: 'Arial', sans-serif;
    font-size: 15pt;
    font-weight: bold;
    letter-spacing: 1px;
    display: block;
    margin-bottom: 2px;
  }
  .corner-box .arabic-jilid {
    font-size: 19pt;
    font-weight: bold;
    display: block;
  }
  .center-kop {
    text-align: center;
  }
  .center-kop .bismillah {
    font-size: 18pt;
    font-weight: bold;
    margin-bottom: 2px;
  }
  .center-kop .instansi {
    font-size: 17pt;
    font-weight: bold;
  }
  .center-kop .judul-ujian {
    font-size: 15pt;
    font-weight: bold;
    color: #222;
  }
  
  /* Garis Pemisah Kop */
  .kop-divider {
    border-top: 3px double #000;
    margin: 6px 0 10px 0;
  }
  
  /* Metadata Info Table */
  .meta-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 14px;
    border: 1px solid #444;
  }
  .meta-table td {
    border: 1px solid #444;
    padding: 4px 8px;
    font-size: 13.5pt;
  }
  .meta-label {
    font-weight: bold;
    background-color: #f4f4f4;
    width: 14%;
  }
  .meta-val {
    width: 36%;
  }
  
  /* Petunjuk & Bagian */
  .section-header {
    background-color: #eee;
    border: 1.5px solid #222;
    padding: 4px 10px;
    margin-top: 14px;
    margin-bottom: 8px;
    font-size: 14.5pt;
    font-weight: bold;
    border-radius: 4px;
  }
  .instructions {
    font-size: 13pt;
    font-style: italic;
    margin-bottom: 8px;
    color: #333;
  }
  
  /* Daftar Soal */
  .question-item {
    margin-bottom: 12px;
    page-break-inside: avoid;
  }
  .question-text {
    font-size: 16.5pt;
    font-weight: bold;
    margin-bottom: 4px;
  }
  .question-options {
    margin-right: 25px;
    margin-top: 4px;
    display: table;
    width: 95%;
  }
  .option-col {
    display: table-cell;
    width: 33.3%;
    font-size: 15pt;
    padding: 2px 6px;
  }
  .answer-dots {
    border-bottom: 1px dotted #555;
    display: inline-block;
    width: 85%;
    margin-right: 8px;
    height: 18px;
  }
  .answer-lines {
    border-bottom: 1px dotted #555;
    margin-top: 18px;
    height: 18px;
  }
  
  @media print {
    body {
      padding: 0;
    }
    .corner-box {
      background-color: transparent !important;
    }
    .section-header {
      background-color: #eee !important;
      -webkit-print-color-adjust: exact;
      print-color-adjust: exact;
    }
  }
</style>
</head>
<body>
<div class="page-container">

  <!-- Kop Surat Ujian -->
  <table class="header-table">
    <tr>
      <!-- Corner Box JILID -->
      <td style="width: 25%; text-align: right;">
        <div class="corner-box">
          <span class="latin-jilid">$latinJilid</span>
          <span class="arabic-jilid">$arabicJilid</span>
        </div>
      </td>
      <!-- Center Kop -->
      <td style="width: 50%; text-align: center;">
        <div class="center-kop">
          <div class="bismillah">بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ</div>
          <div class="instansi">${metadata.namaMadrasah}</div>
          <div class="judul-ujian">${metadata.judulUjian}</div>
        </div>
      </td>
      <!-- Kotak Nilai / Tanggal -->
      <td style="width: 25%; text-align: left;">
        <div class="corner-box" style="font-size: 13pt;">
          <span>الدرجة / النتيجة</span><br>
          <span style="font-size: 18pt; font-weight: bold; display: inline-block; margin-top: 4px;">...... / ١٠٠</span>
        </div>
      </td>
    </tr>
  </table>
  
  <div class="kop-divider"></div>
  
  <!-- Info Ujian -->
  <table class="meta-table">
    <tr>
      <td class="meta-label">الجلد:</td>
      <td class="meta-val"><b>$arabicJilid ($latinJilid)</b></td>
      <td class="meta-label">اسم الطالب:</td>
      <td class="meta-val">...................................................</td>
    </tr>
    <tr>
      <td class="meta-label">المادة:</td>
      <td class="meta-val">${metadata.mataPelajaran}</td>
      <td class="meta-label">رقم الجلوس:</td>
      <td class="meta-val">...................................................</td>
    </tr>
    <tr>
      <td class="meta-label">الليلة:</td>
      <td class="meta-val"><b>${metadata.malamUjian}</b></td>
      <td class="meta-label">التاريخ:</td>
      <td class="meta-val"><b>${metadata.tanggalHijriyah}</b></td>
    </tr>
    <tr>
      <td class="meta-label">الوقت:</td>
      <td class="meta-val">${metadata.durasiWaktu}</td>
      <td class="meta-label">الملاحظة:</td>
      <td class="meta-val">ممنوع فتح الكتب والمذكرات</td>
    </tr>
  </table>
        """.trimIndent())

        if (metadata.modeSoal == "pilihan") {
            // Mode Pilihan Semua
            sb.append("""
  <div class="section-header">القسم العام: أسئلة الاختيار من متعدد (PILIHAN GANDA)</div>
  <div class="instructions">تعليمات: اختر الإجابة الصحيحة بوضع علامة (X) على أحد الخيارات (أ، ب، ج) الآتية:</div>
            """.trimIndent())

            questions.forEachIndexed { index, q ->
                val qNum = toArabicNumber(index + 1)
                val pilA = if (q.pilA.isNotBlank()) q.pilA else "............"
                val pilB = if (q.pilB.isNotBlank()) q.pilB else "............"
                val pilC = if (q.pilC.isNotBlank()) q.pilC else "............"

                sb.append("""
  <div class="question-item">
    <div class="question-text">$qNum. ${q.soal}</div>
    <div class="question-options">
      <div class="option-col"><b>أ.</b> $pilA</div>
      <div class="option-col"><b>ب.</b> $pilB</div>
      <div class="option-col"><b>ج.</b> $pilC</div>
    </div>
  </div>
                """.trimIndent())
            }
        } else {
            // Mode Campur (A: Pilihan Ganda 1-10, B: Meneruskan, C: Melengkapi, D: Isian)
            val pilihanList = questions.filter { it.jenis.lowercase() == "pilihan" }
            val meneruskanList = questions.filter { it.jenis.lowercase() in listOf("meneruskan", "melanjutkan") }
            val melengkapiList = questions.filter { it.jenis.lowercase() == "melengkapi" }
            val isianList = questions.filter { it.jenis.lowercase() in listOf("mengisi", "isian") }

            var overallNumber = 1

            // 1. Bagian A: Pilihan Ganda
            if (pilihanList.isNotEmpty()) {
                sb.append("""
  <div class="section-header">القسم الأول (أ): أسئلة الاختيار من متعدد (PILIHAN GANDA)</div>
  <div class="instructions">تعليمات: اختر الإجابة الصحيحة بوضع علامة (X) على أحد الحروف (أ، ب، ج) المناسبة:</div>
                """.trimIndent())

                pilihanList.forEach { q ->
                    val qNum = toArabicNumber(overallNumber++)
                    sb.append("""
  <div class="question-item">
    <div class="question-text">$qNum. ${q.soal}</div>
    <div class="question-options">
      <div class="option-col"><b>أ.</b> ${q.pilA}</div>
      <div class="option-col"><b>ب.</b> ${q.pilB}</div>
      <div class="option-col"><b>ج.</b> ${q.pilC}</div>
    </div>
  </div>
                    """.trimIndent())
                }
            }

            // 2. Bagian B: Meneruskan / Melanjutkan
            if (meneruskanList.isNotEmpty()) {
                sb.append("""
  <div class="section-header">القسم الثاني (ب): متابعة الجمل والكلمات (MENERUSKAN)</div>
  <div class="instructions">تعليمات: واصل وأكمل الكلمات أو الجمل الآتية إكمالاً صحيحاً:</div>
                """.trimIndent())

                meneruskanList.forEach { q ->
                    val qNum = toArabicNumber(overallNumber++)
                    sb.append("""
  <div class="question-item">
    <div class="question-text">$qNum. ${q.soal} <span class="answer-dots"></span></div>
    <div class="answer-lines"></div>
  </div>
                    """.trimIndent())
                }
            }

            // 3. Bagian C: Melengkapi Kalimat Rumpang
            if (melengkapiList.isNotEmpty()) {
                sb.append("""
  <div class="section-header">القسم الثالث (ج): إكمال الفراغ (MELENGKAPI)</div>
  <div class="instructions">تعليمات: املأ الفراغ بالكلمة المناسبة لتكون الجملة صحيحة وتامة:</div>
                """.trimIndent())

                melengkapiList.forEach { q ->
                    val qNum = toArabicNumber(overallNumber++)
                    sb.append("""
  <div class="question-item">
    <div class="question-text">$qNum. ${q.soal}</div>
    <div style="margin-right: 25px; margin-top: 4px;">الجواب: <span class="answer-dots" style="width: 80%;"></span></div>
  </div>
                    """.trimIndent())
                }
            }

            // 4. Bagian D: Isian Singkat / Pertanyaan Uraian
            if (isianList.isNotEmpty()) {
                sb.append("""
  <div class="section-header">القسم الرابع (د): الأسئela المقالية والإجابات القصيرة (ISIAN / MENGISI)</div>
  <div class="instructions">تعليمات: أجب عن الأسئلة الآتية إجابة واضحة ودقيقة:</div>
                """.trimIndent())

                isianList.forEach { q ->
                    val qNum = toArabicNumber(overallNumber++)
                    sb.append("""
  <div class="question-item">
    <div class="question-text">$qNum. ${q.soal}</div>
    <div style="margin-right: 25px; margin-top: 4px;">الجواب: <span class="answer-dots" style="width: 80%;"></span></div>
    <div class="answer-lines"></div>
  </div>
                    """.trimIndent())
                }
            }

            // Fallback if no specific categories exist yet
            if (questions.isEmpty()) {
                sb.append("""
  <div style="text-align: center; padding: 20px; font-size: 15pt; color: #777;">
    (لا توجد أسئلة مسجلة في هذا الجلد بعد - Belum ada soal terdaftar pada jilid ini)
  </div>
                """.trimIndent())
            }
        }

        sb.append("""
  <br>
  <div style="text-align: center; font-weight: bold; font-size: 14pt; margin-top: 15px; border-top: 1px solid #ccc; padding-top: 8px;">
    مع تمنياتنا لكم بالنجاح والتوفيق - Selamat Mengerjakan & Semoga Sukses
  </div>
</div>
</body>
</html>
        """.trimIndent())

        return sb.toString()
    }

    /**
     * Triggers the Android PrintManager to print or save as PDF (F4 paper size)
     */
    fun printMasterSoal(
        context: Context,
        metadata: ExamMetadata,
        questions: List<QuestionEntity>
    ) {
        val html = generateMasterSoalHtml(metadata, questions)
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager != null) {
                    val jobName = "Master_Soal_${metadata.jilid.replace(" ", "_")}_F4"
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    val attributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.NA_LEGAL) // Closest standard to F4 / Folio
                        .setResolution(PrintAttributes.Resolution("id", "f4_res", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                    printManager.print(jobName, printAdapter, attributes)
                } else {
                    Toast.makeText(context, "Layanan Cetak tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
                }
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    /**
     * Exports the Master Soal as a Microsoft Word compatible (.doc) file
     * with complete RTL Arabic styling and tables.
     */
    fun exportToWordFile(
        context: Context,
        metadata: ExamMetadata,
        questions: List<QuestionEntity>
    ): File {
        val html = generateMasterSoalHtml(metadata, questions)
        val cleanJilid = metadata.jilid.replace(" ", "_").replace("/", "_")
        val fileName = "Master_Soal_${cleanJilid}_F4.doc"
        val exportDir = File(context.cacheDir, "shared_files")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { fos ->
            // Write UTF-8 BOM for Word Arabic character recognition
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            fos.write(html.toByteArray(StandardCharsets.UTF_8))
        }

        return file
    }

    /**
     * Share exported file (Word / Document) or formatted text directly to WhatsApp
     */
    fun shareToWhatsApp(
        context: Context,
        file: File?,
        messageText: String
    ) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            if (file != null && file.exists()) {
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                type = "application/msword"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, messageText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, messageText)
            }
        }

        // Try direct WhatsApp first, fallback to standard chooser
        sendIntent.setPackage("com.whatsapp")
        try {
            context.startActivity(sendIntent)
        } catch (e: Exception) {
            // WhatsApp not found directly, open system app chooser
            sendIntent.setPackage(null)
            val chooser = Intent.createChooser(sendIntent, "Kirim Naskah Soal via...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}
