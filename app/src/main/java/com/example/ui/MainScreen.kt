package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.DualLanguageSelector
import com.example.ui.components.HistoryView
import com.example.ui.components.ModeSelector
import com.example.ui.components.PdfUploadCard
import com.example.ui.components.ResultView
import com.example.ui.components.SummaryLengthSelector
import com.example.util.SourceLanguage
import com.example.util.SummaryLength
import com.example.util.TargetLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val selectedPdf by viewModel.selectedPdf.collectAsStateWithLifecycle()
    val sourceLanguage by viewModel.sourceLanguage.collectAsStateWithLifecycle()
    val targetLanguage by viewModel.targetLanguage.collectAsStateWithLifecycle()
    val summaryLength by viewModel.summaryLength.collectAsStateWithLifecycle()
    val selectedModeIndex by viewModel.selectedModeIndex.collectAsStateWithLifecycle()
    val customQuestion by viewModel.customQuestion.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadingProgressText by viewModel.loadingProgressText.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "مترجم و خلاصه‌ساز PDF",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                        label = { Text("ترجمه فایل") },
                        modifier = Modifier.testTag("nav_workspace_tab")
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = {
                            if (historyList.isNotEmpty()) {
                                BadgedBox(
                                    badge = { Badge { Text("${historyList.size}") } }
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null)
                                }
                            } else {
                                Icon(Icons.Default.History, contentDescription = null)
                            }
                        },
                        label = { Text("تاریخچه") },
                        modifier = Modifier.testTag("nav_history_tab")
                    )
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                if (activeTab == 0) {
                    WorkspaceView(
                        selectedPdf = selectedPdf,
                        sourceLanguage = sourceLanguage,
                        targetLanguage = targetLanguage,
                        summaryLength = summaryLength,
                        selectedModeIndex = selectedModeIndex,
                        customQuestion = customQuestion,
                        isLoading = isLoading,
                        loadingProgressText = loadingProgressText,
                        result = result,
                        errorMessage = errorMessage,
                        onPdfSelected = { uri -> viewModel.selectPdf(uri) },
                        onClearPdf = { viewModel.clearPdf() },
                        onSourceLanguageSelected = { lang -> viewModel.setSourceLanguage(lang) },
                        onTargetLanguageSelected = { lang -> viewModel.setTargetLanguage(lang) },
                        onSummaryLengthSelected = { len -> viewModel.setSummaryLength(len) },
                        onModeSelected = { idx -> viewModel.setModeIndex(idx) },
                        onQuestionChanged = { q -> viewModel.setCustomQuestion(q) },
                        onProcessClick = { viewModel.processPdf() },
                        onDismissError = { viewModel.dismissError() }
                    )
                } else {
                    HistoryView(
                        historyList = historyList,
                        onItemClick = { item -> viewModel.loadHistoryItem(item) },
                        onDeleteItem = { id -> viewModel.deleteHistoryItem(id) },
                        onClearAll = { viewModel.clearAllHistory() },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WorkspaceView(
    selectedPdf: com.example.util.PdfDocumentInfo?,
    sourceLanguage: SourceLanguage,
    targetLanguage: TargetLanguage,
    summaryLength: SummaryLength,
    selectedModeIndex: Int,
    customQuestion: String,
    isLoading: Boolean,
    loadingProgressText: String,
    result: com.example.service.GeminiResult?,
    errorMessage: String?,
    onPdfSelected: (android.net.Uri) -> Unit,
    onClearPdf: () -> Unit,
    onSourceLanguageSelected: (SourceLanguage) -> Unit,
    onTargetLanguageSelected: (TargetLanguage) -> Unit,
    onSummaryLengthSelected: (SummaryLength) -> Unit,
    onModeSelected: (Int) -> Unit,
    onQuestionChanged: (String) -> Unit,
    onProcessClick: () -> Unit,
    onDismissError: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hero Banner Card
        HeroBannerCard()

        // PDF Upload Card
        PdfUploadCard(
            selectedPdf = selectedPdf,
            onPdfSelected = onPdfSelected,
            onClearPdf = onClearPdf
        )

        // Dual Language Selector (Source & Target)
        DualLanguageSelector(
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            onSourceLanguageSelected = onSourceLanguageSelected,
            onTargetLanguageSelected = onTargetLanguageSelected
        )

        // Processing Mode Selector
        ModeSelector(
            selectedIndex = selectedModeIndex,
            onModeSelected = onModeSelected,
            customQuestion = customQuestion,
            onQuestionChanged = onQuestionChanged
        )

        // Summary Length Selector (Shown for Summarize or Both modes)
        AnimatedVisibility(visible = selectedModeIndex == 0 || selectedModeIndex == 2) {
            SummaryLengthSelector(
                selectedLength = summaryLength,
                onLengthSelected = onSummaryLengthSelected
            )
        }

        // Error message banner
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        OutlinedButton(
                            onClick = onDismissError,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("بستن", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Process Action Button
        Button(
            onClick = onProcessClick,
            enabled = selectedPdf != null && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("process_pdf_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "در حال پردازش سند...",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "شروع ترجمه و خلاصه‌سازی PDF",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
        }

        // Loading Progress Card
        AnimatedVisibility(visible = isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "هوش مصنوعی در حال تحلیل است",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = loadingProgressText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Result View (Output)
        AnimatedVisibility(visible = result != null && !isLoading) {
            result?.let { res ->
                ResultView(
                    fileName = selectedPdf?.fileName ?: "Document.pdf",
                    result = res
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HeroBannerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI PRO",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "نسخه ۱.۰",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "PDF AI. TRANS.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    letterSpacing = (-1).sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ترجمه هوشمند و خلاصه‌سازی سریع فایل‌های PDF با خروجی متنی قابل دانلود.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )
            )
        }
    }
}
