package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_history")
data class PdfHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val fileSizeFormatted: String,
    val pageCount: Int,
    val targetLanguageName: String,
    val targetLanguageCode: String,
    val processType: String, // "TRANSLATE", "SUMMARIZE", "BOTH", "QA"
    val originalTextSnippet: String,
    val summaryResult: String,
    val translatedResult: String,
    val timestamp: Long = System.currentTimeMillis()
)
