package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val no: Int,
    val sheetName: String, // e.g. "Jilid 1", "Jilid 2", etc.
    val jenis: String,     // "pilihan", "meneruskan", "melengkapi"
    val soal: String,
    val pilA: String = "",
    val pilB: String = "",
    val pilC: String = "",
    val jawaban: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayJenis: String
        get() = when (jenis.lowercase()) {
            "pilihan" -> "Pilihan Ganda"
            "meneruskan", "melanjutkan" -> "Meneruskan"
            "melengkapi" -> "Melengkapi"
            "mengisi", "isian" -> "Mengisi / Isian"
            else -> jenis.replaceFirstChar { it.uppercase() }
        }
}
