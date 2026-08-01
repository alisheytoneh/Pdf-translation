package com.example.util

data class TargetLanguage(
    val code: String,
    val nameFa: String,
    val nameEn: String,
    val flagEmoji: String
)

data class SourceLanguage(
    val code: String,
    val nameFa: String,
    val nameEn: String,
    val flagEmoji: String
)

object LanguageProvider {
    val defaultTargetLanguage = TargetLanguage("fa", "فارسی (Persian)", "Persian", "🇮🇷")
    val defaultSourceLanguage = SourceLanguage("auto", "شناسایی خودکار", "Auto Detect", "🌐")

    val supportedTargetLanguages = listOf(
        TargetLanguage("fa", "فارسی", "Persian", "🇮🇷"),
        TargetLanguage("en", "انگلیسی", "English", "🇬🇧"),
        TargetLanguage("ar", "عربی", "Arabic", "🇸🇦"),
        TargetLanguage("tr", "ترکی", "Turkish", "🇹🇷"),
        TargetLanguage("de", "آلمانی", "German", "🇩🇪"),
        TargetLanguage("fr", "فرانسوی", "French", "🇫🇷"),
        TargetLanguage("es", "اسپانیایی", "Spanish", "🇪🇸"),
        TargetLanguage("ru", "روسی", "Russian", "🇷🇺"),
        TargetLanguage("zh", "چینی", "Chinese", "🇨🇳"),
        TargetLanguage("ja", "ژاپنی", "Japanese", "🇯🇵"),
        TargetLanguage("it", "ایتالیایی", "Italian", "🇮🇹")
    )

    val supportedSourceLanguages = listOf(
        SourceLanguage("auto", "شناسایی خودکار", "Auto Detect", "🌐"),
        SourceLanguage("en", "انگلیسی", "English", "🇬🇧"),
        SourceLanguage("fa", "فارسی", "Persian", "🇮🇷"),
        SourceLanguage("ar", "عربی", "Arabic", "🇸🇦"),
        SourceLanguage("tr", "ترکی", "Turkish", "🇹🇷"),
        SourceLanguage("de", "آلمانی", "German", "🇩🇪"),
        SourceLanguage("fr", "فرانسوی", "French", "🇫🇷"),
        SourceLanguage("es", "اسپانیایی", "Spanish", "🇪🇸"),
        SourceLanguage("ru", "روسی", "Russian", "🇷🇺"),
        SourceLanguage("zh", "چینی", "Chinese", "🇨🇳"),
        SourceLanguage("ja", "ژاپنی", "Japanese", "🇯🇵"),
        SourceLanguage("it", "ایتالیایی", "Italian", "🇮🇹")
    )
}
