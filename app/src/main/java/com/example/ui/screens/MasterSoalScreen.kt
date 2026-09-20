package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BankSoalViewModel
import com.example.ui.components.ArabicText
import com.example.util.MasterSoalExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterSoalScreen(
    viewModel: BankSoalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allQuestions by viewModel.allQuestions.collectAsState()
    val sheetNames by viewModel.sheetNames.collectAsState()
    val printJilid by viewModel.printJilid.collectAsState()
    val printMode by viewModel.printMode.collectAsState()
    val printMalam by viewModel.printMalam.collectAsState()
    val printTanggalHijriyah by viewModel.printTanggalHijriyah.collectAsState()
    val printMataPelajaran by viewModel.printMataPelajaran.collectAsState()
    val printNamaMadrasah by viewModel.printNamaMadrasah.collectAsState()
    val printDurasi by viewModel.printDurasi.collectAsState()

    val distinctSheets = (listOf("Jilid 1", "Jilid 2", "Jilid 3", "Jilid 4", "Jilid 5") + sheetNames + allQuestions.map { it.sheetName }).distinct().sorted()

    val targetQuestions = allQuestions.filter { it.sheetName == printJilid }
    val arabicJilid = MasterSoalExporter.toArabicJilid(printJilid)
    val jilidDigits = printJilid.filter { it.isDigit() }
    val latinJilid = if (jilidDigits.isNotEmpty()) "JILID $jilidDigits" else printJilid.uppercase()

    var showConfigDetails by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("master_soal_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Control Card: Pilih Jilid & Mode Cetak
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Master Soal Ujian (F4 / Folio)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Cetak otomatis standar ujian madrasah / pesantren",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Pilih Jilid
                    Text(
                        text = "PILIH JILID UJIAN:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        distinctSheets.forEach { sheet ->
                            val count = allQuestions.count { it.sheetName == sheet }
                            FilterChip(
                                selected = printJilid == sheet,
                                onClick = { viewModel.setPrintJilid(sheet) },
                                label = {
                                    Text(
                                        text = "$sheet ($count soal)",
                                        fontWeight = if (printJilid == sheet) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Bentuk Soal (Pilihan Semua atau Campur)
                    Text(
                        text = "BENTUK / FORMAT SOAL:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val modeList = listOf(
                        "campur" to "Campur (Pilihan, Meneruskan, Melengkapi, Isian)",
                        "pilihan" to "Pilihan Ganda Semua (A, B, C)"
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        modeList.forEachIndexed { index, (key, label) ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = modeList.size),
                                onClick = { viewModel.setPrintMode(key) },
                                selected = printMode == key
                            ) {
                                Text(
                                    text = if (key == "campur") "Campur (A, B, C, D)" else "Pilihan Semua",
                                    fontSize = 12.sp,
                                    fontWeight = if (printMode == key) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle Pengaturan Kop & Waktu Arab
                    OutlinedButton(
                        onClick = { showConfigDetails = !showConfigDetails },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showConfigDetails) "Sembunyikan Pengaturan Kop Ujian" else "Atur Malam, Tanggal Hijriyah & Kop Ujian")
                    }

                    AnimatedVisibility(visible = showConfigDetails) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Malam Ujian (الليل)
                            Text(
                                text = "Malam Pelaksanaan Ujian (الليل):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "ليلة الأحد", "ليلة الإثنين", "ليلة الثلاثاء",
                                    "ليلة الأربعاء", "ليلة الخميس", "ليلة الجمعة", "ليلة السبت"
                                ).forEach { night ->
                                    FilterChip(
                                        selected = printMalam == night,
                                        onClick = { viewModel.setPrintMalam(night) },
                                        label = { Text(night, fontFamily = FontFamily.Serif) }
                                    )
                                }
                            }

                            // Tanggal Hijriyah (التاريخ)
                            OutlinedTextField(
                                value = printTanggalHijriyah,
                                onValueChange = { viewModel.setPrintTanggalHijriyah(it) },
                                label = { Text("Tanggal Hijriyah (التاريخ)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Mata Pelajaran (المادة)
                            OutlinedTextField(
                                value = printMataPelajaran,
                                onValueChange = { viewModel.setPrintMataPelajaran(it) },
                                label = { Text("Mata Pelajaran (المادة)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Nama Lembaga (المعهد)
                            OutlinedTextField(
                                value = printNamaMadrasah,
                                onValueChange = { viewModel.setPrintNamaMadrasah(it) },
                                label = { Text("Nama Madrasah / Pesantren") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3 Tombol Utama: Cetak F4, Ekspor Word, Kirim WA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.printMasterSoal(context)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_cetak_f4"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CETAK F4 / PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.exportToWordAndShare(context)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_ekspor_word"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("EKSPOR WORD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.shareMasterSoalWA(context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_kirim_wa"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Official Green
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("KIRIM KE WHATSAPP (WORD & NASKAH)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Live Paper Preview Section (Ukuran F4 Sheet Preview)
        item {
            Text(
                text = "PRATINJAU NASKAH MASTER SOAL F4",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        item {
            // Simulated Paper Sheet (White folio canvas with border)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                border = BorderStroke(1.5.dp, Color(0xFF333333))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header Kop Ujian Table
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Corner Box (Latin JILID & Arabic JILID)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(2.dp, Color.Black),
                            color = Color(0xFFF9F9F9),
                            modifier = Modifier.width(86.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = latinJilid,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = arabicJilid,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                        }

                        // Center Kop
                        Column(
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Text(
                                text = printNamaMadrasah,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "امتحان الفصل الدراسي / بنك الأسئلة الموحد",
                                fontFamily = FontFamily.Serif,
                                fontSize = 12.sp,
                                color = Color(0xFF333333),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Corner Box Nilai
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.5.dp, Color.Black),
                            color = Color(0xFFF9F9F9),
                            modifier = Modifier.width(76.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("الدرجة", fontSize = 11.sp, fontFamily = FontFamily.Serif, color = Color.Black)
                                Text("... / ١٠٠", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 2.dp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Metadata Ujian Grid (RTL)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFF555555)),
                            color = Color(0xFFFDFDFD)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("الجلد: $arabicJilid ($latinJilid)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                    Text("اسم الطالب: ...........................", fontSize = 13.sp, color = Color.Black)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("المادة: $printMataPelajaran", fontSize = 13.sp, color = Color.Black)
                                    Text("رقم الجلوس: ...........................", fontSize = 13.sp, color = Color.Black)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("الليلة: $printMalam", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                    Text("التاريخ: $printTanggalHijriyah", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Content Soal Preview
                    if (targetQuestions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada soal pada $printJilid.\nTambahkan soal terlebih dahulu pada menu Tambah Soal.",
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        val questionsToDisplay = if (printMode == "pilihan") {
                            targetQuestions.filter { it.jenis == "pilihan" }
                        } else {
                            targetQuestions
                        }

                        // Preview Sections
                        if (printMode == "campur") {
                            val pilList = targetQuestions.filter { it.jenis.lowercase() == "pilihan" }
                            val menList = targetQuestions.filter { it.jenis.lowercase() in listOf("meneruskan", "melanjutkan") }
                            val melList = targetQuestions.filter { it.jenis.lowercase() == "melengkapi" }
                            val isiList = targetQuestions.filter { it.jenis.lowercase() in listOf("mengisi", "isian") }

                            var count = 1

                            // Bagian A: Pilihan Ganda
                            if (pilList.isNotEmpty()) {
                                SectionTitleBox("القسم الأول (أ): أسئلة الاختيار من متعدد (PILIHAN GANDA)")
                                pilList.forEach { q ->
                                    PreviewQuestionItem(number = count++, question = q)
                                }
                            }

                            // Bagian B: Meneruskan
                            if (menList.isNotEmpty()) {
                                SectionTitleBox("القسم الثاني (ب): متابعة الجمل والكلمات (MENERUSKAN)")
                                menList.forEach { q ->
                                    PreviewQuestionItem(number = count++, question = q)
                                }
                            }

                            // Bagian C: Melengkapi
                            if (melList.isNotEmpty()) {
                                SectionTitleBox("القسم الثالث (ج): إكمال الفراغ (MELENGKAPI)")
                                melList.forEach { q ->
                                    PreviewQuestionItem(number = count++, question = q)
                                }
                            }

                            // Bagian D: Isian
                            if (isiList.isNotEmpty()) {
                                SectionTitleBox("القسم الرابع (د): الأسئلة المقالية (ISIAN / MENGISI)")
                                isiList.forEach { q ->
                                    PreviewQuestionItem(number = count++, question = q)
                                }
                            }
                        } else {
                            SectionTitleBox("القسم العام: أسئلة الاختيار من متعدد (PILIHAN GANDA SEMUA)")
                            questionsToDisplay.forEachIndexed { index, q ->
                                PreviewQuestionItem(number = index + 1, question = q)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFF888888))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "مع تمنياتنا لكم بالنجاح والتوفيق - Ukuran Kertas Master: F4 (215 x 330 mm)",
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitleBox(title: String) {
    Surface(
        color = Color(0xFFEEEEEE),
        border = BorderStroke(1.dp, Color(0xFF333333)),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            textAlign = TextAlign.Right
        )
    }
}

@Composable
private fun PreviewQuestionItem(
    number: Int,
    question: com.example.data.model.QuestionEntity
) {
    val arabicNum = MasterSoalExporter.toArabicNumber(number)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            // Soal text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "$arabicNum. ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color.Black
                )
                Text(
                    text = question.soal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color.Black,
                    lineHeight = 24.sp
                )
            }

            // Options or lines
            if (question.jenis.lowercase() == "pilihan") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "أ. ${question.pilA}",
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF222222)
                    )
                    Text(
                        text = "ب. ${question.pilB}",
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF222222)
                    )
                    Text(
                        text = "ج. ${question.pilC}",
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF222222)
                    )
                }
            } else {
                Text(
                    text = "الجواب: .....................................................................................................",
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 4.dp, start = 20.dp)
                )
            }
        }
    }
}
