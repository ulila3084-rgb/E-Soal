package com.example.data.repository

import com.example.data.local.QuestionDao
import com.example.data.model.QuestionEntity
import com.example.data.remote.GoogleAppsScriptService
import com.example.data.remote.InitialDataResult
import com.example.data.remote.PostResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class QuestionRepository(
    private val questionDao: QuestionDao,
    private val gasService: GoogleAppsScriptService = GoogleAppsScriptService()
) {

    fun getAllQuestions(): Flow<List<QuestionEntity>> = questionDao.getAllQuestions()

    fun getAllSheetNames(): Flow<List<String>> = questionDao.getAllSheetNames()

    fun searchQuestions(
        query: String,
        sheetName: String = "Semua",
        jenis: String = "Semua"
    ): Flow<List<QuestionEntity>> = questionDao.searchQuestions(query, sheetName, jenis)

    suspend fun getNextQuestionNumber(sheetName: String): Int {
        val max = questionDao.getMaxQuestionNumber(sheetName) ?: 0
        return max + 1
    }

    suspend fun addQuestion(question: QuestionEntity, scriptUrl: String? = null): Long {
        val id = questionDao.insertQuestion(question)
        if (!scriptUrl.isNullOrBlank()) {
            try {
                gasService.postQuestion(scriptUrl, question)
            } catch (_: Exception) {
                // Keep local save even if remote fails
            }
        }
        return id
    }

    suspend fun updateQuestion(question: QuestionEntity) {
        questionDao.updateQuestion(question)
    }

    suspend fun deleteQuestion(question: QuestionEntity, scriptUrl: String? = null) {
        questionDao.deleteQuestion(question)
        if (!scriptUrl.isNullOrBlank()) {
            try {
                gasService.deleteQuestionRemote(scriptUrl, question.sheetName, question.no + 1)
            } catch (_: Exception) {
                // Ignore remote error on delete
            }
        }
    }

    suspend fun getStats(): Map<String, Int> {
        val sheets = questionDao.getAllSheetNames().first()
        val stats = mutableMapOf<String, Int>()
        for (sheet in sheets) {
            stats[sheet] = questionDao.getCountBySheet(sheet)
        }
        return stats
    }

    suspend fun syncWithScript(scriptUrl: String): Result<InitialDataResult> {
        return gasService.fetchInitialData(scriptUrl)
    }

    suspend fun seedInitialDataIfNeeded() {
        val currentQuestions = questionDao.getAllQuestions().first()
        if (currentQuestions.isEmpty()) {
            val sampleQuestions = listOf(
                // Jilid 1 - Pilihan Ganda (Dari prompt pengguna)
                QuestionEntity(
                    no = 1,
                    sheetName = "Jilid 1",
                    jenis = "pilihan",
                    soal = "ياغ تر ماسوك تاندايا كلمة فعل",
                    pilA = "قد",
                    pilB = "ال",
                    pilC = "بيسا دي جير كن",
                    jawaban = "أ. قد"
                ),
                QuestionEntity(
                    no = 2,
                    sheetName = "Jilid 1",
                    jenis = "pilihan",
                    soal = "اداله فيباب بروباهيا اخر كلمة",
                    pilA = "عامل",
                    pilB = "مبني",
                    pilC = "معراب",
                    jawaban = "أ. عامل"
                ),
                QuestionEntity(
                    no = 3,
                    sheetName = "Jilid 1",
                    jenis = "pilihan",
                    soal = "لفظ ياغ دي اخري الف دان تاء اياله",
                    pilA = "اسم تثنية",
                    pilB = "اسم مفرد",
                    pilC = "جمع مؤنث سالم",
                    jawaban = "ج. جمع مؤنث سالم"
                ),
                QuestionEntity(
                    no = 4,
                    sheetName = "Jilid 1",
                    jenis = "pilihan",
                    soal = "فعل ياغ مبني ادا دوا",
                    pilA = "ماض امر",
                    pilB = "مضارع امر",
                    pilC = "فعل مضارع",
                    jawaban = "أ. ماض امر"
                ),
                QuestionEntity(
                    no = 5,
                    sheetName = "Jilid 1",
                    jenis = "pilihan",
                    soal = "ستياف كاتا ياغ دي اولي حرف جير اداله كلمة",
                    pilA = "اسم",
                    pilB = "فعل",
                    pilC = "حرف",
                    jawaban = "أ. اسم"
                ),

                // Jilid 1 - Meneruskan & Mengisi (Campuran Jilid 1)
                QuestionEntity(
                    no = 6,
                    sheetName = "Jilid 1",
                    jenis = "meneruskan",
                    soal = "الكلام هو اللفظ المركب",
                    jawaban = "المفيد بالوضع"
                ),
                QuestionEntity(
                    no = 7,
                    sheetName = "Jilid 1",
                    jenis = "melengkapi",
                    soal = "أقسام الكلام ثلاثة: اسم، وفعل، و______",
                    jawaban = "حرف جاء لمعنى"
                ),
                QuestionEntity(
                    no = 8,
                    sheetName = "Jilid 1",
                    jenis = "mengisi",
                    soal = "ما هو تعريف الاسم في علم النحو؟",
                    jawaban = "كلمة دلت على معنى في نفسها ولم تقترن بزمان وضعاً"
                ),

                // Jilid 2 - Meneruskan
                QuestionEntity(
                    no = 1,
                    sheetName = "Jilid 2",
                    jenis = "meneruskan",
                    soal = "إنما الأعمال",
                    jawaban = "بالنيات وإنما لكل امرئ ما نوى"
                ),
                QuestionEntity(
                    no = 2,
                    sheetName = "Jilid 2",
                    jenis = "meneruskan",
                    soal = "طلب العلم فريضة",
                    jawaban = "على كل مسلم"
                ),
                QuestionEntity(
                    no = 3,
                    sheetName = "Jilid 2",
                    jenis = "meneruskan",
                    soal = "من سلك طريقا يلتمس فيه علما",
                    jawaban = "سهل الله له به طريقا إلى الجنة"
                ),
                QuestionEntity(
                    no = 4,
                    sheetName = "Jilid 2",
                    jenis = "mengisi",
                    soal = "اذكر أركان الصلاة المفروضة بالتفصيل؟",
                    jawaban = "النية، والقيام، وتكبيرة الإحرام، وقراءة الفاتحة..."
                ),

                // Jilid 3 - Melengkapi
                QuestionEntity(
                    no = 1,
                    sheetName = "Jilid 3",
                    jenis = "melengkapi",
                    soal = "ذهب الطالب إلى ______",
                    jawaban = "المدرسة"
                ),
                QuestionEntity(
                    no = 2,
                    sheetName = "Jilid 3",
                    jenis = "melengkapi",
                    soal = "يقرأ الأستاذ ______ في الفصل",
                    jawaban = "الكتاب"
                ),
                QuestionEntity(
                    no = 3,
                    sheetName = "Jilid 3",
                    jenis = "melengkapi",
                    soal = "القرآن الكريم نزل على النبي ______",
                    jawaban = "محمد صلى الله عليه وسلم"
                )
            )
            questionDao.insertAll(sampleQuestions)
        }
    }
}
