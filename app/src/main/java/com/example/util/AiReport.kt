package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Report of one AI answer, sent by email so a person reviews it (Play AI-generated content policy). */
object AiReport {
    const val SUPPORT_EMAIL = "cryptocycles11@gmail.com"

    fun subject(greek: Boolean): String =
        if (greek) "Αναφορά απάντησης AI (CryptoCycles)" else "AI answer report (CryptoCycles)"

    fun body(messageId: String, timestampMs: Long, answer: String, greek: Boolean): String {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm 'UTC'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(timestampMs))
        val intro = if (greek) "Τι είναι λάθος ή ακατάλληλο σε αυτή την απάντηση;" else "What is wrong or inappropriate in this answer?"
        return "$intro\n\n\n---\nID: $messageId\n$time\n\n$answer"
    }
}
