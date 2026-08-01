package com.example.util

enum class SummaryLength(
    val id: String,
    val titleFa: String,
    val subtitleFa: String
) {
    SHORT(
        id = "short",
        titleFa = "کوتاه",
        subtitleFa = "خلاصه کلیدی در چند سطر و بولت‌پوینت"
    ),
    MEDIUM(
        id = "medium",
        titleFa = "متوسط",
        subtitleFa = "خلاصه متوازن همراه با تمام نکات اصلی"
    ),
    DETAILED(
        id = "detailed",
        titleFa = "بلند و جامع",
        subtitleFa = "خلاصه تفصیلی همراه با تحلیل کامل بخش‌ها"
    )
}
