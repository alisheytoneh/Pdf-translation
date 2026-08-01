package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class PdfDocumentInfo(
    val uri: Uri,
    val fileName: String,
    val fileSizeFormatted: String,
    val sizeInBytes: Long,
    val pageCount: Int,
    val base64Data: String,
    val thumbnailBitmap: Bitmap? = null
)

object PdfReaderHelper {

    suspend fun readPdfFromUri(context: Context, uri: Uri): Result<PdfDocumentInfo> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            
            // Extract file name and size
            var fileName = "Document.pdf"
            var fileSize = 0L
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: "Document.pdf"
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            // Read byte array
            val inputStream: InputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Could not open PDF file stream"))
            
            val bytes = inputStream.readBytes()
            inputStream.close()

            if (bytes.isEmpty()) {
                return@withContext Result.failure(Exception("PDF file is empty"))
            }

            val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val sizeFormatted = formatFileSize(bytes.size.toLong())

            // Create temporary file for PdfRenderer
            val tempFile = File.createTempFile("pdf_preview", ".pdf", context.cacheDir)
            val fos = FileOutputStream(tempFile)
            fos.write(bytes)
            fos.close()

            var pageCount = 1
            var thumbnail: Bitmap? = null

            try {
                val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                pageCount = renderer.pageCount
                if (pageCount > 0) {
                    val page = renderer.openPage(0)
                    val width = 300
                    val height = (width * (page.height.toFloat() / page.width.toFloat())).toInt().coerceAtLeast(300)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    thumbnail = bitmap
                }
                renderer.close()
                pfd.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                tempFile.delete()
            }

            Result.success(
                PdfDocumentInfo(
                    uri = uri,
                    fileName = fileName,
                    fileSizeFormatted = sizeFormatted,
                    sizeInBytes = bytes.size.toLong(),
                    pageCount = pageCount,
                    base64Data = base64String,
                    thumbnailBitmap = thumbnail
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
