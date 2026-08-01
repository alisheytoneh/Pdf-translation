package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PdfHistoryEntity
import com.example.data.PdfHistoryRepository
import com.example.service.GeminiResult
import com.example.service.GeminiService
import com.example.service.ProcessingType
import com.example.util.LanguageProvider
import com.example.util.PdfDocumentInfo
import com.example.util.PdfReaderHelper
import com.example.util.SourceLanguage
import com.example.util.SummaryLength
import com.example.util.TargetLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PdfHistoryRepository

    init {
        val dao = AppDatabase.getDatabase(application).pdfHistoryDao()
        repository = PdfHistoryRepository(dao)
    }

    val historyList: StateFlow<List<PdfHistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedPdf = MutableStateFlow<PdfDocumentInfo?>(null)
    val selectedPdf: StateFlow<PdfDocumentInfo?> = _selectedPdf.asStateFlow()

    private val _sourceLanguage = MutableStateFlow<SourceLanguage>(LanguageProvider.defaultSourceLanguage)
    val sourceLanguage: StateFlow<SourceLanguage> = _sourceLanguage.asStateFlow()

    private val _targetLanguage = MutableStateFlow<TargetLanguage>(LanguageProvider.defaultTargetLanguage)
    val targetLanguage: StateFlow<TargetLanguage> = _targetLanguage.asStateFlow()

    private val _summaryLength = MutableStateFlow<SummaryLength>(SummaryLength.MEDIUM)
    val summaryLength: StateFlow<SummaryLength> = _summaryLength.asStateFlow()

    private val _selectedModeIndex = MutableStateFlow(0) // 0: Both, 1: Translate, 2: Summarize, 3: Q&A
    val selectedModeIndex: StateFlow<Int> = _selectedModeIndex.asStateFlow()

    private val _customQuestion = MutableStateFlow("")
    val customQuestion: StateFlow<String> = _customQuestion.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingProgressText = MutableStateFlow("")
    val loadingProgressText: StateFlow<String> = _loadingProgressText.asStateFlow()

    private val _result = MutableStateFlow<GeminiResult?>(null)
    val result: StateFlow<GeminiResult?> = _result.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: Main Workspace, 1: History
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    fun selectPdf(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingProgressText.value = "در حال خواندن فایل PDF..."
            _errorMessage.value = null
            _result.value = null

            val result = PdfReaderHelper.readPdfFromUri(getApplication(), uri)
            _isLoading.value = false

            result.onSuccess { info ->
                _selectedPdf.value = info
            }.onFailure { exception ->
                _errorMessage.value = "خطا در بارگذاری فایل PDF: ${exception.localizedMessage}"
            }
        }
    }

    fun clearPdf() {
        _selectedPdf.value = null
        _result.value = null
        _errorMessage.value = null
    }

    fun setSourceLanguage(language: SourceLanguage) {
        _sourceLanguage.value = language
    }

    fun setTargetLanguage(language: TargetLanguage) {
        _targetLanguage.value = language
    }

    fun setSummaryLength(length: SummaryLength) {
        _summaryLength.value = length
    }

    fun setModeIndex(index: Int) {
        _selectedModeIndex.value = index
    }

    fun setCustomQuestion(question: String) {
        _customQuestion.value = question
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    fun processPdf() {
        val pdf = _selectedPdf.value ?: return
        val sourceLang = _sourceLanguage.value
        val targetLang = _targetLanguage.value
        val length = _summaryLength.value

        val processingType: ProcessingType = when (_selectedModeIndex.value) {
            0 -> ProcessingType.Both(length)
            1 -> ProcessingType.Translate
            2 -> ProcessingType.Summarize(length)
            3 -> ProcessingType.CustomQuestion(_customQuestion.value.ifBlank { "لطفاً خلاصه و تحلیل اصلی این سند را توضیح دهید." })
            else -> ProcessingType.Both(length)
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _result.value = null

            _loadingProgressText.value = "در حال خلاصه سازی و ترجمه فایل PDF توسط هوش مصنوعی..."

            val geminiResult = GeminiService.processPdf(
                pdfBase64 = pdf.base64Data,
                sourceLanguage = sourceLang,
                targetLanguage = targetLang,
                processingType = processingType
            )

            _isLoading.value = false

            if (geminiResult.error != null) {
                _errorMessage.value = geminiResult.error
            } else {
                _result.value = geminiResult

                val modeTitle = when (_selectedModeIndex.value) {
                    0 -> "ترجمه و خلاصه‌سازی"
                    1 -> "ترجمه کامل"
                    2 -> "خلاصه سازی (${length.titleFa})"
                    3 -> "پرسش و پاسخ"
                    else -> "پردازش"
                }

                val historyItem = PdfHistoryEntity(
                    fileName = pdf.fileName,
                    fileSizeFormatted = pdf.fileSizeFormatted,
                    pageCount = pdf.pageCount,
                    targetLanguageName = targetLang.nameFa,
                    targetLanguageCode = targetLang.code,
                    processType = modeTitle,
                    originalTextSnippet = pdf.fileName,
                    summaryResult = geminiResult.summary,
                    translatedResult = geminiResult.combinedMarkdown
                )

                repository.save(historyItem)
            }
        }
    }

    fun loadHistoryItem(item: PdfHistoryEntity) {
        _result.value = GeminiResult(
            summary = item.summaryResult,
            translation = item.translatedResult,
            combinedMarkdown = item.translatedResult
        )
        _activeTab.value = 0
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clear()
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}
