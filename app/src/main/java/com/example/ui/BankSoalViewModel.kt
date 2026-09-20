package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BankSoalDatabase
import com.example.data.model.QuestionEntity
import com.example.data.repository.QuestionRepository
import com.example.util.MasterSoalExporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BankSoalViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BankSoalDatabase.getDatabase(application)
    private val repository = QuestionRepository(database.questionDao())

    val defaultScriptUrl = "https://script.google.com/macros/s/AKfycbz_fjgj5UEP3YF6mHLH0TV54CIh54Ri6cDzeNEeoQxFJCnat_zAB4oJ5WCE5OU4ArJDPg/exec"
    private val _scriptUrl = MutableStateFlow(defaultScriptUrl)
    val scriptUrl: StateFlow<String> = _scriptUrl.asStateFlow()

    // Navigation Tab (0: Home/Daftar, 1: Tambah Soal, 2: Cetak Master F4, 3: Latihan/Quiz, 4: Google Sheets Sync)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setCurrentTab(tab: Int) {
        _currentTab.value = tab
    }

    // Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedJilidFilter = MutableStateFlow("Semua")
    val selectedJilidFilter: StateFlow<String> = _selectedJilidFilter.asStateFlow()

    private val _selectedJenisFilter = MutableStateFlow("Semua")
    val selectedJenisFilter: StateFlow<String> = _selectedJenisFilter.asStateFlow()

    // Sheets list from DB
    val sheetNames: StateFlow<List<String>> = repository.getAllSheetNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Jilid 1", "Jilid 2", "Jilid 3"))

    // Filtered questions
    @OptIn(ExperimentalCoroutinesApi::class)
    val questions: StateFlow<List<QuestionEntity>> = combine(
        _searchQuery,
        _selectedJilidFilter,
        _selectedJenisFilter
    ) { query, jilid, jenis ->
        Triple(query, jilid, jenis)
    }.flatMapLatest { (query, jilid, jenis) ->
        repository.searchQuestions(query, jilid, jenis)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All questions for statistics
    val allQuestions: StateFlow<List<QuestionEntity>> = repository.getAllQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Loading & Messages
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    // Add Form State
    private val _inputJilid = MutableStateFlow("Jilid 1")
    val inputJilid: StateFlow<String> = _inputJilid.asStateFlow()

    private val _newJilidName = MutableStateFlow("")
    val newJilidName: StateFlow<String> = _newJilidName.asStateFlow()

    private val _isCustomJilid = MutableStateFlow(false)
    val isCustomJilid: StateFlow<Boolean> = _isCustomJilid.asStateFlow()

    private val _inputJenis = MutableStateFlow("pilihan") // "pilihan", "meneruskan", "melengkapi", "mengisi"
    val inputJenis: StateFlow<String> = _inputJenis.asStateFlow()

    private val _inputSoal = MutableStateFlow("")
    val inputSoal: StateFlow<String> = _inputSoal.asStateFlow()

    private val _inputPilA = MutableStateFlow("")
    val inputPilA: StateFlow<String> = _inputPilA.asStateFlow()

    private val _inputPilB = MutableStateFlow("")
    val inputPilB: StateFlow<String> = _inputPilB.asStateFlow()

    private val _inputPilC = MutableStateFlow("")
    val inputPilC: StateFlow<String> = _inputPilC.asStateFlow()

    private val _inputJawabanPilihan = MutableStateFlow("أ")
    val inputJawabanPilihan: StateFlow<String> = _inputJawabanPilihan.asStateFlow()

    private val _inputJawabanLain = MutableStateFlow("")
    val inputJawabanLain: StateFlow<String> = _inputJawabanLain.asStateFlow()

    // Last focused input field for Arabic virtual bar insertion
    var activeInputTarget: String = "soal"

    // Quiz Mode State
    private val _quizJilid = MutableStateFlow("Semua")
    val quizJilid: StateFlow<String> = _quizJilid.asStateFlow()

    private val _quizQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val quizQuestions: StateFlow<List<QuestionEntity>> = _quizQuestions.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _selectedQuizAnswer = MutableStateFlow<String?>(null)
    val selectedQuizAnswer: StateFlow<String?> = _selectedQuizAnswer.asStateFlow()

    private val _showQuizExplanation = MutableStateFlow(false)
    val showQuizExplanation: StateFlow<Boolean> = _showQuizExplanation.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizFinished = MutableStateFlow(false)
    val quizFinished: StateFlow<Boolean> = _quizFinished.asStateFlow()

    // Sync State
    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    // ==========================================
    // Master Soal F4 Print & Export States
    // ==========================================
    private val _printJilid = MutableStateFlow("Jilid 1")
    val printJilid: StateFlow<String> = _printJilid.asStateFlow()

    private val _printMode = MutableStateFlow("campur") // "campur" or "pilihan"
    val printMode: StateFlow<String> = _printMode.asStateFlow()

    private val _printMalam = MutableStateFlow("ليلة الأحد")
    val printMalam: StateFlow<String> = _printMalam.asStateFlow()

    private val _printTanggalHijriyah = MutableStateFlow("١٥ ربيع الأول ١٤٤٨ هـ")
    val printTanggalHijriyah: StateFlow<String> = _printTanggalHijriyah.asStateFlow()

    private val _printMataPelajaran = MutableStateFlow("النحو والصرف والقراءة")
    val printMataPelajaran: StateFlow<String> = _printMataPelajaran.asStateFlow()

    private val _printNamaMadrasah = MutableStateFlow("المعهد الإسلامي / مدرسة التوفيق لتعليم القرآن والعلوم الشرعية")
    val printNamaMadrasah: StateFlow<String> = _printNamaMadrasah.asStateFlow()

    private val _printDurasi = MutableStateFlow("٩٠ دقيقة")
    val printDurasi: StateFlow<String> = _printDurasi.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    // Filter Setters
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedJilidFilter(jilid: String) {
        _selectedJilidFilter.value = jilid
    }

    fun setSelectedJenisFilter(jenis: String) {
        _selectedJenisFilter.value = jenis
    }

    fun setScriptUrl(url: String) {
        _scriptUrl.value = url.trim()
    }

    // Form Setters
    fun setInputJilid(jilid: String) {
        _inputJilid.value = jilid
        _isCustomJilid.value = false
    }

    fun setNewJilidName(name: String) {
        _newJilidName.value = name
    }

    fun setIsCustomJilid(custom: Boolean) {
        _isCustomJilid.value = custom
    }

    fun setInputJenis(jenis: String) {
        _inputJenis.value = jenis
    }

    fun setInputSoal(soal: String) {
        _inputSoal.value = soal
    }

    fun setInputPilA(pilA: String) {
        _inputPilA.value = pilA
    }

    fun setInputPilB(pilB: String) {
        _inputPilB.value = pilB
    }

    fun setInputPilC(pilC: String) {
        _inputPilC.value = pilC
    }

    fun setInputJawabanPilihan(jawaban: String) {
        _inputJawabanPilihan.value = jawaban
    }

    fun setInputJawabanLain(jawaban: String) {
        _inputJawabanLain.value = jawaban
    }

    fun appendArabicChar(char: String) {
        when (activeInputTarget) {
            "soal" -> _inputSoal.value += char
            "pilA" -> _inputPilA.value += char
            "pilB" -> _inputPilB.value += char
            "pilC" -> _inputPilC.value += char
            "jawabanLain" -> _inputJawabanLain.value += char
            else -> _inputSoal.value += char
        }
    }

    fun setupQuickAdd(jilid: String, jenis: String) {
        _inputJilid.value = jilid
        _isCustomJilid.value = false
        _inputJenis.value = jenis
        resetForm(keepSettings = true)
        _currentTab.value = 1
    }

    fun resetForm(keepSettings: Boolean = false) {
        _inputSoal.value = ""
        _inputPilA.value = ""
        _inputPilB.value = ""
        _inputPilC.value = ""
        _inputJawabanLain.value = ""
        if (!keepSettings) {
            _inputJenis.value = "pilihan"
            _inputJawabanPilihan.value = "أ"
            _newJilidName.value = ""
            _isCustomJilid.value = false
        }
    }

    fun saveQuestion(
        onSuccess: (Int, String) -> Unit,
        onError: (String) -> Unit
    ) {
        val targetJilid = if (_isCustomJilid.value) {
            _newJilidName.value.trim()
        } else {
            _inputJilid.value.trim()
        }

        if (targetJilid.isBlank()) {
            onError("Silakan pilih atau isi nama Jilid!")
            return
        }

        val soal = _inputSoal.value.trim()
        if (soal.isBlank()) {
            onError("Silakan isi teks soal terlebih dahulu.")
            return
        }

        val jenis = _inputJenis.value
        val finalJawaban: String
        val pilA = _inputPilA.value.trim()
        val pilB = _inputPilB.value.trim()
        val pilC = _inputPilC.value.trim()

        if (jenis == "pilihan") {
            if (pilA.isBlank() || pilB.isBlank() || pilC.isBlank()) {
                onError("Semua pilihan A, B, dan C wajib diisi!")
                return
            }
            finalJawaban = when (_inputJawabanPilihan.value) {
                "أ" -> "أ. $pilA"
                "ب" -> "ب. $pilB"
                "ج" -> "ج. $pilC"
                else -> _inputJawabanPilihan.value
            }
        } else {
            val jawabanLain = _inputJawabanLain.value.trim()
            if (jawabanLain.isBlank()) {
                onError("Jawaban tidak boleh kosong!")
                return
            }
            finalJawaban = jawabanLain
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val nextNo = repository.getNextQuestionNumber(targetJilid)
                val newQuestion = QuestionEntity(
                    no = nextNo,
                    sheetName = targetJilid,
                    jenis = jenis,
                    soal = soal,
                    pilA = pilA,
                    pilB = pilB,
                    pilC = pilC,
                    jawaban = finalJawaban
                )
                repository.addQuestion(newQuestion, _scriptUrl.value)
                resetForm(keepSettings = true)
                _toastMessage.value = "Soal No. $nextNo berhasil ditambahkan ke $targetJilid!"
                onSuccess(nextNo, targetJilid)
            } catch (e: Exception) {
                onError("Gagal menyimpan: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteQuestion(question, _scriptUrl.value)
                _toastMessage.value = "Soal No. ${question.no} (${question.sheetName}) berhasil dihapus."
            } catch (e: Exception) {
                _toastMessage.value = "Gagal menghapus: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==========================================
    // Master Soal F4 Setters & Actions
    // ==========================================
    fun setPrintJilid(jilid: String) {
        _printJilid.value = jilid
    }

    fun setPrintMode(mode: String) {
        _printMode.value = mode
    }

    fun setPrintMalam(malam: String) {
        _printMalam.value = malam
    }

    fun setPrintTanggalHijriyah(tanggal: String) {
        _printTanggalHijriyah.value = tanggal
    }

    fun setPrintMataPelajaran(mapel: String) {
        _printMataPelajaran.value = mapel
    }

    fun setPrintNamaMadrasah(madrasah: String) {
        _printNamaMadrasah.value = madrasah
    }

    fun setPrintDurasi(durasi: String) {
        _printDurasi.value = durasi
    }

    fun getPrintExamMetadata(): MasterSoalExporter.ExamMetadata {
        return MasterSoalExporter.ExamMetadata(
            jilid = _printJilid.value,
            namaMadrasah = _printNamaMadrasah.value,
            mataPelajaran = _printMataPelajaran.value,
            malamUjian = _printMalam.value,
            tanggalHijriyah = _printTanggalHijriyah.value,
            durasiWaktu = _printDurasi.value,
            modeSoal = _printMode.value
        )
    }

    fun getQuestionsForCurrentPrint(): List<QuestionEntity> {
        val targetJilid = _printJilid.value
        val list = allQuestions.value.filter { it.sheetName == targetJilid }
        return if (_printMode.value == "pilihan") {
            list.filter { it.jenis.lowercase() == "pilihan" }
        } else {
            list
        }
    }

    fun printMasterSoal(context: Context) {
        val metadata = getPrintExamMetadata()
        val questions = getQuestionsForCurrentPrint()
        MasterSoalExporter.printMasterSoal(context, metadata, questions)
    }

    fun exportToWordAndShare(context: Context) {
        val metadata = getPrintExamMetadata()
        val questions = getQuestionsForCurrentPrint()
        val file = MasterSoalExporter.exportToWordFile(context, metadata, questions)
        val jilidArabic = MasterSoalExporter.toArabicJilid(metadata.jilid)
        val msg = "Naskah Master Soal Ujian F4 - ${metadata.jilid} ($jilidArabic)\n${metadata.malamUjian}, ${metadata.tanggalHijriyah}\nMapel: ${metadata.mataPelajaran}"
        MasterSoalExporter.shareToWhatsApp(context, file, msg)
        _toastMessage.value = "Naskah Master Soal Word siap dibagikan ke WhatsApp!"
    }

    fun shareMasterSoalWA(context: Context) {
        val metadata = getPrintExamMetadata()
        val questions = getQuestionsForCurrentPrint()
        val jilidArabic = MasterSoalExporter.toArabicJilid(metadata.jilid)

        val textSummary = buildString {
            append("📝 *MASTER SOAL UJIAN F4 - ${metadata.jilid} ($jilidArabic)*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("🏫 ${metadata.namaMadrasah}\n")
            append("📖 Mata Pelajaran: ${metadata.mataPelajaran}\n")
            append("🌙 Waktu Ujian: ${metadata.malamUjian}\n")
            append("📅 Tanggal Hijriyah: ${metadata.tanggalHijriyah}\n")
            append("⏱ Durasi: ${metadata.durasiWaktu}\n")
            append("📋 Mode Soal: ${if (metadata.modeSoal == "pilihan") "Pilihan Ganda Semua" else "Campur (Pilihan, Meneruskan, Melengkapi, Isian)"}\n")
            append("🔢 Total Soal: ${questions.size} Soal\n\n")

            questions.take(15).forEachIndexed { idx, q ->
                append("${idx + 1}. [${q.displayJenis}] ${q.soal}\n")
                if (q.jenis == "pilihan") {
                    append("   أ. ${q.pilA} | ب. ${q.pilB} | ج. ${q.pilC}\n")
                }
            }
            if (questions.size > 15) {
                append("\n... dan ${questions.size - 15} soal lainnya.\n")
            }
            append("\n_Dibuat otomatis via Aplikasi Bank Soal_")
        }

        val file = MasterSoalExporter.exportToWordFile(context, metadata, questions)
        MasterSoalExporter.shareToWhatsApp(context, file, textSummary)
    }

    // ==========================================
    // Quiz / Latihan Actions
    // ==========================================
    fun startQuiz(jilid: String = "Semua") {
        _quizJilid.value = jilid
        val pool = if (jilid == "Semua") {
            allQuestions.value.shuffled()
        } else {
            allQuestions.value.filter { it.sheetName == jilid }.shuffled()
        }
        _quizQuestions.value = pool
        _currentQuizIndex.value = 0
        _selectedQuizAnswer.value = null
        _showQuizExplanation.value = false
        _quizScore.value = 0
        _quizFinished.value = pool.isEmpty()
    }

    fun submitQuizAnswer(answer: String) {
        if (_showQuizExplanation.value) return
        _selectedQuizAnswer.value = answer
        _showQuizExplanation.value = true

        val current = _quizQuestions.value.getOrNull(_currentQuizIndex.value) ?: return
        val isCorrect = when (current.jenis) {
            "pilihan" -> {
                val expectedLetter = when {
                    current.jawaban.startsWith("أ") -> "أ"
                    current.jawaban.startsWith("ب") -> "ب"
                    current.jawaban.startsWith("ج") -> "ج"
                    else -> current.jawaban
                }
                answer == expectedLetter || current.jawaban.contains(answer)
            }
            else -> {
                answer.trim() == current.jawaban.trim()
            }
        }
        if (isCorrect) {
            _quizScore.value += 1
        }
    }

    fun nextQuizQuestion() {
        if (_currentQuizIndex.value + 1 < _quizQuestions.value.size) {
            _currentQuizIndex.value += 1
            _selectedQuizAnswer.value = null
            _showQuizExplanation.value = false
        } else {
            _quizFinished.value = true
        }
    }

    fun syncWithGoogleScript() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "Menghubungkan ke Google Apps Script..."
            try {
                val result = repository.syncWithScript(_scriptUrl.value)
                result.fold(
                    onSuccess = { data ->
                        if (data.isHtml) {
                            _syncStatus.value = "Web App Aktif! URL terhubung normal. Mode data lokal & cloud aktif."
                        } else {
                            _syncStatus.value = "Berhasil! Ditemukan ${data.sheets.size} sheet dari spreadsheet."
                        }
                        _toastMessage.value = "Koneksi Google Script berhasil!"
                    },
                    onFailure = { error ->
                        _syncStatus.value = "Koneksi belum berhasil: ${error.localizedMessage}"
                        _toastMessage.value = "Periksa koneksi atau deployment URL"
                    }
                )
            } catch (e: Exception) {
                _syncStatus.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
