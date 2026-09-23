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
            assertEquals(language.name, 22, chapters.size)
            chapters.forEachIndexed { index, chapter ->
                assertEquals(index + 1, chapter.id)
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
        assertTrue(en[17].title.contains("liquidation", ignoreCase = true))
        assertTrue(en[18].title.contains("Funding", ignoreCase = true))
        assertTrue(en[19].title.contains("Order book", ignoreCase = true))
        assertFalse(en[19].title.contains("Quantum", ignoreCase = true))
        assertTrue(en[20].title.contains("Halving", ignoreCase = true))
        assertTrue(en[20].title.contains("issuance", ignoreCase = true))
        assertTrue(en[21].title.contains("venue", ignoreCase = true))

        val el = BlockchainLearnRepository.getChapters(AppLanguage.GREEK)
        assertTrue(el[17].title.contains("ρευστοποίησης", ignoreCase = true))
        assertTrue(el[18].title.contains("Funding", ignoreCase = true))
        assertTrue(el[19].title.contains("Βιβλίο", ignoreCase = true))
        assertTrue(el[20].title.contains("halving", ignoreCase = true))
        assertTrue(el[21].title.contains("ανταλλακτήριο", ignoreCase = true))
    }
}
