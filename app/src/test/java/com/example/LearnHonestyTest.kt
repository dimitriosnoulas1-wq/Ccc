package com.example

import com.example.data.model.AppLanguage
import com.example.data.repository.BlockchainLearnRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearnHonestyTest {

    private val languages = AppLanguage.entries

    @Test
    fun twentyTwoChaptersAndProGateMatchesReality() {
        languages.forEach { language ->
            val chapters = BlockchainLearnRepository.getChapters(language)
            val intro = chapters.filter { it.id in 101..105 }
            val core = chapters.filter { it.id in 1..22 }
            assertEquals(language.name, 5, intro.size)
            assertEquals(language.name, 22, core.size)
            assertEquals(language.name, 27, chapters.size)
            intro.forEach { chapter ->
                assertFalse("intro ${chapter.id} ${language.name} should be free", chapter.isProOnly)
                assertTrue(chapter.title.contains("read", ignoreCase = true) || chapter.title.contains("διαβάζ", ignoreCase = true) || chapter.title.contains("Lesen") || chapter.title.contains("Lire") || chapter.title.contains("Cómo") || chapter.title.contains("legge"))
            }
            core.forEach { chapter ->
                if (chapter.id <= 17) {
                    assertFalse("chapter ${chapter.id} ${language.name} should be free", chapter.isProOnly)
                } else {
                    assertTrue("chapter ${chapter.id} ${language.name} should be Pro", chapter.isProOnly)
                }
            }
        }
    }

    @Test
    fun eachLessonHasReadableLength() {
        languages.forEach { language ->
            BlockchainLearnRepository.getChapters(language).forEach { chapter ->
                val paragraphs = chapter.content.split("\n\n").filter { it.isNotBlank() }
                assertTrue(
                    "${language.name} ${chapter.id} needs at least 4 paragraphs, had ${paragraphs.size}",
                    paragraphs.size >= 4
                )
                assertTrue(
                    "${language.name} ${chapter.id} body is still short (${chapter.content.length})",
                    chapter.content.length >= 480
                )
                assertTrue(chapter.realExample.length >= 40)
                assertTrue(chapter.commonMistake.length >= 40)
            }
        }
    }

    @Test
    fun proLessonsAreMechanicsNotTradeCalls() {
        val banned = listOf(
            "12-18",
            "12 to 18",
            "12 έως 18",
            "12 bis 18",
            "12 à 18",
            "12 a 18",
            "12 e 18",
            "1-2%",
            "1–2%",
            "1-2 %",
            "81%",
            "Quantum Order Flow",
            "Liquidation Engineering",
            "mean-reversion",
            "turning signals",
            "αξιόπιστους δείκτες",
            "Kontra-Indikatoren",
            "signaux clés de retournement",
            "señales clave",
            "punti d'inversione",
            "+0.05%",
            "+0.08%",
            "+0.10%",
            "+0,08%",
            "+0,05%",
            "3-4%",
            "1.000 BTC",
            "\$50M",
            "capital allocation",
            "στρατηγικό σχεδιασμό",
            "Masterclass"
        )
        languages.forEach { language ->
            BlockchainLearnRepository.getChapters(language).forEach { chapter ->
                val blob = listOf(
                    chapter.title,
                    chapter.content,
                    chapter.diagramCaption,
                    chapter.diagramExtraNote.orEmpty(),
                    chapter.realExample,
                    chapter.commonMistake
                ).joinToString("\n")
                banned.forEach { needle ->
                    assertFalse(
                        "${language.name} ${chapter.id} still contains '$needle'",
                        blob.contains(needle, ignoreCase = true)
                    )
                }
            }
        }
    }

    @Test
    fun englishAndGreekProTitlesNameTheMechanic() {
        val en = BlockchainLearnRepository.getChapters(AppLanguage.ENGLISH)
        assertTrue(en.first { it.id == 18 }.title.contains("liquidation", ignoreCase = true))
        assertTrue(en.first { it.id == 19 }.title.contains("Funding", ignoreCase = true))
        assertTrue(en.first { it.id == 20 }.title.contains("Order book", ignoreCase = true))
        assertFalse(en.first { it.id == 20 }.title.contains("Quantum", ignoreCase = true))
        assertTrue(en.first { it.id == 21 }.title.contains("Halving", ignoreCase = true))
        assertTrue(en.first { it.id == 21 }.title.contains("issuance", ignoreCase = true))
        assertTrue(en.first { it.id == 22 }.title.contains("venue", ignoreCase = true))

        val el = BlockchainLearnRepository.getChapters(AppLanguage.GREEK)
        assertTrue(el.first { it.id == 18 }.title.contains("ρευστοποίησης", ignoreCase = true))
        assertTrue(el.first { it.id == 19 }.title.contains("Funding", ignoreCase = true))
        assertTrue(el.first { it.id == 20 }.title.contains("Βιβλίο", ignoreCase = true))
        assertTrue(el.first { it.id == 21 }.title.contains("halving", ignoreCase = true))
        assertTrue(el.first { it.id == 22 }.title.contains("ανταλλακτήριο", ignoreCase = true))
    }
}
