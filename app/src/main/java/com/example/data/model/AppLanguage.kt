package com.example.data.model

enum class AppLanguage(val code: String, val nativeName: String, val englishName: String) {
    ENGLISH("en", "English", "Default"),
    GERMAN("de", "Deutsch", "German"),
    FRENCH("fr", "Français", "French"),
    SPANISH("es", "Español", "Spanish"),
    ITALIAN("it", "Italiano", "Italian"),
    GREEK("el", "Ελληνικά", "Greek")
}
