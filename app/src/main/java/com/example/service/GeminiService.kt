package com.example.service

import com.example.BuildConfig
import com.example.util.SourceLanguage
import com.example.util.SummaryLength
import com.example.util.TargetLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class ProcessingType {
    object Translate : ProcessingType()
    data class Summarize(val length: SummaryLength) : ProcessingType()
    data class Both(val length: SummaryLength) : ProcessingType()
    data class CustomQuestion(val question: String) : ProcessingType()
}

data class GeminiResult(
    val summary: String = "",
    val translation: String = "",
    val combinedMarkdown: String = "",
    val error: String? = null
)

object GeminiService {

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun processPdf(
        pdfBase64: String,
        sourceLanguage: SourceLanguage,
        targetLanguage: TargetLanguage,
        processingType: ProcessingType
    ): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiResult(
                error = "کلید API Gemini یافت نشد. لطفاً کلید API را در پانل Secrets تنظیم کنید."
            )
        }

        val prompt = buildPrompt(sourceLanguage, targetLanguage, processingType)

        try {
            val root = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // Text Prompt Part
            val promptPart = JSONObject()
            promptPart.put("text", prompt)
            partsArray.put(promptPart)

            // PDF Inline Data Part
            val pdfPart = JSONObject()
            val inlineData = JSONObject()
            inlineData.put("mimeType", "application/pdf")
            inlineData.put("data", pdfBase64)
            pdfPart.put("inlineData", inlineData)
            partsArray.put(pdfPart)

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.3)
            root.put("generationConfig", genConfig)

            val requestUrl = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = root.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(requestUrl)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = parseErrorMessage(responseBodyString)
                return@withContext GeminiResult(error = "خطا در برقراری ارتباط با مدل Gemini ($errorMsg)")
            }

            val textResponse = parseGeminiTextResponse(responseBodyString)
            if (textResponse.isBlank()) {
                return@withContext GeminiResult(error = "پاسخی از مدل دریافت نشد.")
            }

            when (processingType) {
                is ProcessingType.Translate -> {
                    GeminiResult(
                        translation = textResponse,
                        combinedMarkdown = textResponse
                    )
                }
                is ProcessingType.Summarize -> {
                    GeminiResult(
                        summary = textResponse,
                        combinedMarkdown = textResponse
                    )
                }
                is ProcessingType.Both -> {
                    val split = splitSummaryAndTranslation(textResponse)
                    GeminiResult(
                        summary = split.first,
                        translation = split.second,
                        combinedMarkdown = textResponse
                    )
                }
                is ProcessingType.CustomQuestion -> {
                    GeminiResult(
                        combinedMarkdown = textResponse
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            GeminiResult(error = "خطا در پردازش: ${e.localizedMessage}")
        }
    }

    private fun buildPrompt(
        sourceLang: SourceLanguage,
        targetLang: TargetLanguage,
        type: ProcessingType
    ): String {
        val targetName = "${targetLang.nameFa} (${targetLang.nameEn})"
        val sourceInfo = if (sourceLang.code == "auto") {
            "Auto-detect source language"
        } else {
            "Source language: ${sourceLang.nameFa} (${sourceLang.nameEn})"
        }

        return when (type) {
            is ProcessingType.Translate -> """
                You are an expert document translator.
                $sourceInfo.
                Task: Translate the entire attached PDF document into $targetName.
                Requirements:
                - Preserve formatting, headers, lists, and structure.
                - Deliver high quality, natural $targetName text.
                - Output only the clean translated document in well-formatted Markdown.
            """.trimIndent()

            is ProcessingType.Summarize -> {
                val lengthInstruction = getSummaryLengthInstruction(type.length)
                """
                    You are an expert document analyst.
                    $sourceInfo.
                    Task: 
                    1. Read and analyze the attached PDF document.
                    2. Generate a structured summary of the PDF content.
                    3. Translate and write the final summary strictly in $targetName.

                    Summary Depth/Length: ${type.length.titleFa}
                    $lengthInstruction

                    Format using clean Markdown with headers, bold text, and bullet points in $targetName.
                """.trimIndent()
            }

            is ProcessingType.Both -> {
                val lengthInstruction = getSummaryLengthInstruction(type.length)
                """
                    You are a master document analyst and translator.
                    $sourceInfo.
                    Task: Process the attached PDF document in two steps:
                    Step 1: Generate a summary of the PDF content according to the requested depth level, and translate this summary into $targetName.
                    Step 2: Provide a complete translation of the full document into $targetName.

                    Summary Depth/Length: ${type.length.titleFa}
                    $lengthInstruction

                    Format output strictly into TWO sections as follows:

                    SECTION 1:
                    # 📑 خلاصه ترجمه شده سند (Summary)
                    [Insert translated summary here]

                    SECTION 2:
                    # 🌐 ترجمه کامل سند (Full Content)
                    [Insert full translated document text here]
                """.trimIndent()
            }

            is ProcessingType.CustomQuestion -> """
                You are an AI document assistant.
                $sourceInfo.
                Task: Read the attached PDF document carefully and answer the user's question strictly in $targetName:

                User Question: "${type.question}"

                Provide a clear, accurate, and detailed answer in $targetName.
            """.trimIndent()
        }
    }

    private fun getSummaryLengthInstruction(length: SummaryLength): String {
        return when (length) {
            SummaryLength.SHORT -> """
                - Keep the summary brief and concise (around 100-150 words).
                - Use 3-5 key bullet points for executive reading.
            """.trimIndent()
            SummaryLength.MEDIUM -> """
                - Provide a balanced summary (around 250-350 words).
                - Include executive summary, main findings, and key takeaways.
            """.trimIndent()
            SummaryLength.DETAILED -> """
                - Provide a detailed and comprehensive summary covering all sections, core arguments, methodologies, findings, and conclusions.
                - Include section-by-section breakdown where applicable.
            """.trimIndent()
        }
    }

    private fun parseGeminiTextResponse(jsonString: String): String {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""

            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                sb.append(part.optString("text", ""))
            }
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseErrorMessage(jsonString: String): String {
        return try {
            val root = JSONObject(jsonString)
            val error = root.optJSONObject("error")
            error?.optString("message", "Unknown error") ?: "HTTP Error"
        } catch (e: Exception) {
            "Network response error"
        }
    }

    private fun splitSummaryAndTranslation(fullText: String): Pair<String, String> {
        val translateHeader = "# 🌐 ترجمه کامل سند"
        if (fullText.contains(translateHeader)) {
            val parts = fullText.split(translateHeader)
            if (parts.size >= 2) {
                return Pair(parts[0].trim(), parts[1].trim())
            }
        }
        return Pair(fullText, fullText)
    }
}
