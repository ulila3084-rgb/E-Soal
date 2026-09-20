package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY sheetName ASC, no ASC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE sheetName = :sheetName ORDER BY no ASC")
    fun getQuestionsBySheet(sheetName: String): Flow<List<QuestionEntity>>

    @Query("SELECT DISTINCT sheetName FROM questions ORDER BY sheetName ASC")
    fun getAllSheetNames(): Flow<List<String>>

    @Query("SELECT MAX(no) FROM questions WHERE sheetName = :sheetName")
    suspend fun getMaxQuestionNumber(sheetName: String): Int?

    @Query("""
        SELECT * FROM questions 
        WHERE (:sheetName = 'Semua' OR sheetName = :sheetName)
        AND (:jenis = 'Semua' OR LOWER(jenis) = LOWER(:jenis))
        AND (
            :query = '' 
            OR CAST(no AS TEXT) = :query 
            OR soal LIKE '%' || :query || '%' 
            OR jawaban LIKE '%' || :query || '%'
            OR pilA LIKE '%' || :query || '%'
            OR pilB LIKE '%' || :query || '%'
            OR pilC LIKE '%' || :query || '%'
        )
        ORDER BY sheetName ASC, no ASC
    """)
    fun searchQuestions(
        query: String,
        sheetName: String = "Semua",
        jenis: String = "Semua"
    ): Flow<List<QuestionEntity>>

    @Query("SELECT COUNT(*) FROM questions WHERE sheetName = :sheetName")
    suspend fun getCountBySheet(sheetName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM questions")
    suspend fun clearAll()
}
