package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ExportHelper {

    fun copyToClipboard(context: Context, text: String, label: String = "Translated Text") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "متن با موفقیت کپی شد!", Toast.LENGTH_SHORT).show()
    }

    fun shareText(context: Context, text: String, title: String = "اشتراک‌گذاری متن ترجمه شده") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }

    fun exportAsTextFile(context: Context, fileName: String, content: String) {
        try {
            val cleanName = fileName.replace(".pdf", "", ignoreCase = true)
                .replace("[^a-zA-Z0-9_.-]".toRegex(), "_")
            val outputFileName = "Translated_$cleanName.txt"

            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val file = File(exportDir, outputFileName)
            val fos = FileOutputStream(file)
            fos.write(content.toByteArray(Charsets.UTF_8))
            fos.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, outputFileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "ذخیره یا خروجی گرفتن فایل متنی")
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطا در خروجی گرفتن: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
