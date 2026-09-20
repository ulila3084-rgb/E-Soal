package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Bank Soal", appName)
  }

  @Test
  fun `verify arabic number and jilid formatting`() {
    assertEquals("١", com.example.util.MasterSoalExporter.toArabicNumber(1))
    assertEquals("١٠", com.example.util.MasterSoalExporter.toArabicNumber(10))
    assertEquals("جلد ١", com.example.util.MasterSoalExporter.toArabicJilid("Jilid 1"))
    assertEquals("جلد ٢", com.example.util.MasterSoalExporter.toArabicJilid("Jilid 2"))
  }

  @Test
  fun `verify master soal html generation f4`() {
    val metadata = com.example.util.MasterSoalExporter.ExamMetadata(
      jilid = "Jilid 1",
      malamUjian = "ليلة الأحد",
      tanggalHijriyah = "١٥ ربيع الأول ١٤٤٨ هـ",
      modeSoal = "campur"
    )
    val questions = listOf(
      com.example.data.model.QuestionEntity(
        no = 1,
        sheetName = "Jilid 1",
        jenis = "pilihan",
        soal = "ما هو الكلام؟",
        pilA = "اللفظ المركب",
        pilB = "المفرد",
        pilC = "الحرف",
        jawaban = "أ. اللفظ المركب"
      )
    )
    val html = com.example.util.MasterSoalExporter.generateMasterSoalHtml(metadata, questions)
    org.junit.Assert.assertTrue(html.contains("215mm 330mm"))
    org.junit.Assert.assertTrue(html.contains("dir=\"rtl\""))
    org.junit.Assert.assertTrue(html.contains("JILID 1"))
    org.junit.Assert.assertTrue(html.contains("جلد ١"))
    org.junit.Assert.assertTrue(html.contains("ليلة الأحد"))
  }
}
