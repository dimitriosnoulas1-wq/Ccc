package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.BlockchainChapter
import com.example.data.repository.BlockchainLearnRepository
import com.example.ui.components.ChapterDiagram
import com.example.ui.theme.CopperAccent
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.JetBrainsMonoFont
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.QuantumCornerReticleOverlay
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.SpaceGroteskFont
import com.example.ui.theme.SyneFont
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.holographicCard
import com.example.ui.theme.stitchHorizonPanel
import com.example.ui.components.QuantumReticleBadge
import com.example.util.AppSoundManager
import kotlinx.coroutines.launch

// Terminal palette (Void black & Etched glass HUD)
private val LearnBg = CosmicVoidBg
private val LearnCardBg = CosmicVoidSurface
private val LearnCardBorder = CosmicBorder
private val LearnExampleBg = CosmicVoidSurfaceElevated
private val LearnExampleBorder = CosmicBorder
private val LearnMistakeBg = CosmicVoidSurface
private val LearnMistakeBorder = SoftCrimson.copy(alpha = 0.40f)

private val CyanAccent = QuantumCyan
private val CrimsonAccent = SoftCrimson
private val AmberPrimary = PhotonGold
private val AmberGlow = PhotonGold
private val MintAccent = TachyonMint

private val TextPrimaryHighContrast = Color(0xFFF1F5F9)
private val TextSecondaryComfort = Color(0xFFCBD5E1)
private val TextMutedComfort = Color(0xFF8DA0B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnBlockchainScreen(
    currentLanguage: AppLanguage,
    isProUnlocked: Boolean = false,
    onOpenProModal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val chapters = remember(currentLanguage) { BlockchainLearnRepository.getChapters(currentLanguage) }
    var currentChapterIndex by rememberSaveable { mutableIntStateOf(0) }
    var showChapterPickerSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Smooth reset scroll on chapter change
    LaunchedEffect(currentChapterIndex) {
        scrollState.scrollTo(0)
    }

    val currentChapter = chapters.getOrElse(currentChapterIndex) { chapters.first() }
    val isGreek = currentLanguage == AppLanguage.GREEK
    val isCurrentChapterLocked = currentChapter.isProOnly && !isProUnlocked

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LearnBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Top Progress & Chapter Quick Selector
            LearnTopHeader(
                currentIndex = currentChapterIndex,
                totalChapters = chapters.size,
                isProUnlocked = isProUnlocked,
                onOpenChapterPicker = {
                    AppSoundManager.playTechClick()
                    showChapterPickerSheet = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Main Chapter Content (Animated & Anti-fatigue)
            AnimatedContent(
                targetState = currentChapter to isCurrentChapterLocked,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "LearnChapterTransition",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { (chapter, isLocked) ->
                if (isLocked) {
                    // Locked Pro Gatekeeper for Chapters 2..17
                    LockedChapterGatekeeper(
                        chapter = chapter,
                        language = currentLanguage,
                        onUnlockPro = {
                            AppSoundManager.playTechClick()
                            onOpenProModal()
                        },
                        onBackToFree = {
                            AppSoundManager.playTechClick()
                            currentChapterIndex = 0
                        }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = 110.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Top Chapter Title (Large, high contrast, clean)
                        ChapterHeaderTitle(
                            chapter = chapter,
                            isFree = chapter.id == 1,
                            language = currentLanguage
                        )

                        // Center: Large Custom Diagram (big shapes, empty space, 1 accent color)
                        ChapterDiagram(
                            diagramType = chapter.diagramType,
                            language = currentLanguage
                        )

                        // Diagram Caption
                        if (chapter.diagramCaption.isNotBlank()) {
                            Text(
                                text = chapter.diagramCaption,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = TextMutedComfort,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        // Below: 3-5 Short Bite-Sized Paragraphs (Clean line breaks, no walls of text)
                        ParagraphsSection(content = chapter.content)

                        // Two Small Clean Cards: Example and Common Mistake
                        ExampleCard(
                            example = chapter.realExample,
                            language = currentLanguage
                        )

                        CommonMistakeCard(
                            mistake = chapter.commonMistake,
                            language = currentLanguage
                        )

                        // Subtle educational disclaimer
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.GREEK -> "Εκπαιδευτικό υλικό · Όχι επενδυτική συμβουλή"
                                AppLanguage.GERMAN -> "Lernmaterial · Keine Finanzberatung"
                                AppLanguage.FRENCH -> "Matériel éducatif · Non un conseil financier"
                                AppLanguage.SPANISH -> "Material educativo · No es asesoramiento financiero"
                                AppLanguage.ITALIAN -> "Materiale didattico · Non è una consulenza finanziaria"
                                AppLanguage.ENGLISH -> "Educational material · Not financial advice"
                            },
                            fontSize = 11.sp,
                            color = TextMutedComfort,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 3. Big Next / Back Buttons at Bottom
            val nextIndex = currentChapterIndex + 1
            val isNextChapterLocked = nextIndex < chapters.size && (chapters.getOrNull(nextIndex)?.isProOnly == true) && !isProUnlocked

            LearnBottomNavBar(
                currentIndex = currentChapterIndex,
                totalChapters = chapters.size,
                language = currentLanguage,
                isProUnlocked = isProUnlocked,
                isNextLocked = isNextChapterLocked,
                onPrevious = {
                    if (currentChapterIndex > 0) {
                        AppSoundManager.playTechClick()
                        currentChapterIndex--
                    }
                },
                onNext = {
                    val targetChapter = chapters.getOrNull(currentChapterIndex + 1)
                    if (targetChapter != null && targetChapter.isProOnly && !isProUnlocked) {
                        // User is attempting to enter Pro chapter (Chapters 18-22)
                        AppSoundManager.playTechClick()
                        onOpenProModal()
                    } else if (currentChapterIndex < chapters.size - 1) {
                        AppSoundManager.playTechClick()
                        currentChapterIndex++
                    }
                }
            )

            // Spacer for FAB clearance (FAB height + 16dp)
            Spacer(modifier = Modifier.height(72.dp))
        }

        // Table of contents modal sheet
        if (showChapterPickerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showChapterPickerSheet = false },
                sheetState = sheetState,
                containerColor = LearnCardBg
            ) {
                ChapterPickerSheetContent(
                    chapters = chapters,
                    currentIndex = currentChapterIndex,
                    language = currentLanguage,
                    isProUnlocked = isProUnlocked,
                    onSelectChapter = { idx ->
                        val targetChapter = chapters.getOrNull(idx)
                        if (targetChapter != null && targetChapter.isProOnly && !isProUnlocked) {
                            AppSoundManager.playTechClick()
                            coroutineScope.launch {
                                sheetState.hide()
                                showChapterPickerSheet = false
                            }
                            onOpenProModal()
                        } else {
                            AppSoundManager.playTechClick()
                            currentChapterIndex = idx
                            coroutineScope.launch {
                                sheetState.hide()
                                showChapterPickerSheet = false
                            }
                        }
                    },
                    onClose = {
                        coroutineScope.launch {
                            sheetState.hide()
                            showChapterPickerSheet = false
                        }
                    }
                )
            }
        }
    }
}

// ---------------------- SUB-COMPONENTS ----------------------

@Composable
private fun LearnTopHeader(
    currentIndex: Int,
    totalChapters: Int,
    isProUnlocked: Boolean,
    onOpenChapterPicker: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .stitchHorizonPanel(shape = RoundedCornerShape(14.dp), borderWidth = 1.2.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Progress indicator with 22 subtle dots + counter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF140D2E))
                    .border(1.dp, QuantumCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(QuantumCyan)
                    )
                    Text(
                        text = "${currentIndex + 1}/$totalChapters",
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMonoFont,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Visual progress dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until totalChapters) {
                    val isActive = i <= currentIndex
                    val isCurrent = i == currentIndex
                    val isLocked = i >= 17 && !isProUnlocked
                    Box(
                        modifier = Modifier
                            .size(
                                width = if (isCurrent) 10.dp else 4.5.dp,
                                height = 4.5.dp
                            )
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    isCurrent -> QuantumCyan
                                    isActive -> QuantumCyan.copy(alpha = 0.75f)
                                    isLocked -> MauveAurora.copy(alpha = 0.3f)
                                    else -> Color(0xFF2A2050)
                                }
                            )
                    )
                }
            }
        }

        // Table of Contents trigger button (INDEX)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF140D2E))
                .border(1.dp, QuantumCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .clickable {
                    AppSoundManager.playTechClick()
                    onOpenChapterPicker()
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("btn_chapter_picker"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoStories,
                contentDescription = "Index",
                tint = QuantumCyan,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "INDEX",
                fontSize = 11.5.sp,
                fontFamily = SpaceGroteskFont,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ChapterHeaderTitle(
    chapter: BlockchainChapter,
    isFree: Boolean,
    language: AppLanguage
) {
    val chapterWord = when (language) {
        AppLanguage.GREEK -> "ΚΕΦΑΛΑΙΟ ${chapter.id}"
        AppLanguage.GERMAN -> "KAPITEL ${chapter.id}"
        AppLanguage.FRENCH -> "CHAPITRE ${chapter.id}"
        AppLanguage.SPANISH -> "CAPÍTULO ${chapter.id}"
        AppLanguage.ITALIAN -> "CAPITOLO ${chapter.id}"
        AppLanguage.ENGLISH -> "CHAPTER ${chapter.id}"
    }

    val freeBadge = when (language) {
        AppLanguage.GREEK -> "ΔΩΡΕΑΝ"
        AppLanguage.GERMAN -> "GRATIS"
        AppLanguage.FRENCH -> "GRATUIT"
        AppLanguage.SPANISH -> "GRATIS"
        AppLanguage.ITALIAN -> "GRATIS"
        AppLanguage.ENGLISH -> "FREE"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "BLOCKCHAIN",
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFont,
                    fontWeight = FontWeight.Bold,
                    color = QuantumCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "·",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MauveAurora
                )
                Text(
                    text = chapterWord,
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFont,
                    fontWeight = FontWeight.Bold,
                    color = MauveAurora,
                    letterSpacing = 1.sp
                )
            }

            if (isFree) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TachyonMint.copy(alpha = 0.15f))
                        .border(1.dp, TachyonMint.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = freeBadge,
                        fontSize = 9.5.sp,
                        fontFamily = JetBrainsMonoFont,
                        fontWeight = FontWeight.Bold,
                        color = TachyonMint
                    )
                }
            }
        }

        Text(
            text = chapter.title,
            fontSize = 20.sp,
            fontFamily = SpaceGroteskFont,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 26.sp
        )
    }
}

@Composable
private fun ParagraphsSection(content: String) {
    // Split into 3-5 bite-sized paragraphs
    val paragraphs = content.split("\n\n").filter { it.isNotBlank() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        paragraphs.forEach { paragraph ->
            Text(
                text = paragraph.trim(),
                fontSize = 15.sp,
                color = TextPrimaryHighContrast,
                lineHeight = 23.sp
            )
        }
    }
}

@Composable
private fun ExampleCard(
    example: String,
    language: AppLanguage
) {
    val headerTitle = when (language) {
        AppLanguage.GREEK -> "Παράδειγμα"
        AppLanguage.GERMAN -> "Beispiel"
        AppLanguage.FRENCH -> "Exemple"
        AppLanguage.SPANISH -> "Ejemplo"
        AppLanguage.ITALIAN -> "Esempio"
        AppLanguage.ENGLISH -> "Example"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D0A1D))
            .border(1.dp, QuantumCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .height(IntrinsicSize.Min)
    ) {
        // Soft Cyan vertical accent bar on the left edge
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(CyanAccent, CyanAccent.copy(alpha = 0.60f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = headerTitle,
                    fontSize = 12.5.sp,
                    fontFamily = SpaceGroteskFont,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
            }

            Text(
                text = example,
                fontSize = 13.5.sp,
                color = TextSecondaryComfort,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun CommonMistakeCard(
    mistake: String,
    language: AppLanguage
) {
    val headerTitle = when (language) {
        AppLanguage.GREEK -> "Συχνό λάθος"
        AppLanguage.GERMAN -> "Häufiger Irrtum"
        AppLanguage.FRENCH -> "Erreur fréquente"
        AppLanguage.SPANISH -> "Error común"
        AppLanguage.ITALIAN -> "Errore comune"
        AppLanguage.ENGLISH -> "Common mistake"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D0A1D))
            .border(1.dp, SoftCrimson.copy(alpha = 0.40f), RoundedCornerShape(12.dp))
            .height(IntrinsicSize.Min)
    ) {
        // Soft Crimson vertical accent bar on the left edge
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(CrimsonAccent, CrimsonAccent.copy(alpha = 0.60f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = CrimsonAccent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = headerTitle,
                    fontSize = 12.5.sp,
                    fontFamily = SpaceGroteskFont,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonAccent
                )
            }

            Text(
                text = mistake,
                fontSize = 13.5.sp,
                color = TextSecondaryComfort,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun LockedChapterGatekeeper(
    chapter: BlockchainChapter,
    language: AppLanguage,
    onUnlockPro: () -> Unit,
    onBackToFree: () -> Unit
) {
    val lockTitle = when (language) {
        AppLanguage.GREEK -> "Κεφάλαια 2–17: Έκδοση Pro"
        AppLanguage.GERMAN -> "Kapitel 2–17: Pro Version"
        AppLanguage.FRENCH -> "Chapitres 2–17 : Version Pro"
        AppLanguage.SPANISH -> "Capítulos 2–17: Versión Pro"
        AppLanguage.ITALIAN -> "Capitoli 2–17: Versione Pro"
        AppLanguage.ENGLISH -> "Chapters 2–17: Pro Tier"
    }

    val lockDesc = when (language) {
        AppLanguage.GREEK -> "Το Κεφάλαιο 1 παρέχεται δωρεάν. Ξεκλείδωσε ολόκληρη τη σειρά 17 κεφαλαίων Blockchain, μαζί με όλα τα Macro, Futures και Whale σήματα του CryptoCycles."
        AppLanguage.GERMAN -> "Kapitel 1 ist kostenlos. Schalte alle 17 Blockchain-Kapitel sowie alle Macro-, Futures- und Whale-Signale von CryptoCycles frei."
        AppLanguage.FRENCH -> "Le chapitre 1 est offert. Débloquez les 17 chapitres Blockchain ainsi que tous les signaux Macro, Futures et Whales de CryptoCycles."
        AppLanguage.SPANISH -> "El Capítulo 1 es gratuito. Desbloquea los 17 capítulos de Blockchain junto con todas las señales Macro, Futures y Whales de CryptoCycles."
        AppLanguage.ITALIAN -> "Il Capitolo 1 è gratuito. Sblocca tutti i 17 capitoli Blockchain insieme a tutti i segnali Macro, Futures e Whales di CryptoCycles."
        AppLanguage.ENGLISH -> "Chapter 1 is free. Unlock the complete 17-chapter Blockchain course along with all Macro, Futures, and Whale signals in CryptoCycles."
    }

    val unlockBtnText = when (language) {
        AppLanguage.GREEK -> "Ξεκλείδωμα Pro Πρόσβασης"
        AppLanguage.GERMAN -> "Pro Zugang freischalten"
        AppLanguage.FRENCH -> "Débloquer l'accès Pro"
        AppLanguage.SPANISH -> "Desbloquear Acceso Pro"
        AppLanguage.ITALIAN -> "Sblocca Accesso Pro"
        AppLanguage.ENGLISH -> "Unlock Pro Access"
    }

    val backFreeText = when (language) {
        AppLanguage.GREEK -> "Επιστροφή στο Κεφάλαιο 1 (Δωρεάν)"
        AppLanguage.GERMAN -> "Zurück zu Kapitel 1 (Gratis)"
        AppLanguage.FRENCH -> "Retour au Chapitre 1 (Gratuit)"
        AppLanguage.SPANISH -> "Volver al Capítulo 1 (Gratis)"
        AppLanguage.ITALIAN -> "Torna al Capitolo 1 (Gratis)"
        AppLanguage.ENGLISH -> "Back to Chapter 1 (Free)"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LearnCardBg)
                .border(1.dp, CopperAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(CopperAccent.copy(alpha = 0.15f))
                        .border(1.dp, CopperAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = CopperAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = chapter.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryHighContrast,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = lockTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CopperAccent,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = lockDesc,
                    fontSize = 13.5.sp,
                    color = TextSecondaryComfort,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        AppSoundManager.playTechClick()
                        onUnlockPro()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberPrimary,
                        contentColor = Color(0xFF05050F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, AmberGlow.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .testTag("btn_learn_unlock_pro")
                ) {
                    Icon(
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = unlockBtnText,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        AppSoundManager.playTechClick()
                        onBackToFree()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Color(0xFF070F1C).copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                        .testTag("btn_learn_back_free")
                ) {
                    Text(
                        text = backFreeText,
                        fontSize = 13.sp,
                        color = TextPrimaryHighContrast
                    )
                }
            }
        }
    }
}

@Composable
private fun LearnBottomNavBar(
    currentIndex: Int,
    totalChapters: Int,
    language: AppLanguage,
    isProUnlocked: Boolean,
    isNextLocked: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val backText = when (language) {
        AppLanguage.GREEK -> "Πίσω"
        AppLanguage.GERMAN -> "Zurück"
        AppLanguage.FRENCH -> "Retour"
        AppLanguage.SPANISH -> "Atrás"
        AppLanguage.ITALIAN -> "Indietro"
        AppLanguage.ENGLISH -> "Back"
    }

    val nextText = when {
        isNextLocked -> when (language) {
            AppLanguage.GREEK -> "Επόμενο (PRO)"
            AppLanguage.GERMAN -> "Weiter (PRO)"
            AppLanguage.FRENCH -> "Suivant (PRO)"
            AppLanguage.SPANISH -> "Siguiente (PRO)"
            AppLanguage.ITALIAN -> "Avanti (PRO)"
            AppLanguage.ENGLISH -> "Next (PRO)"
        }
        currentIndex == totalChapters - 1 -> when (language) {
            AppLanguage.GREEK -> "Ολοκλήρωση"
            AppLanguage.GERMAN -> "Fertigstellen"
            AppLanguage.FRENCH -> "Terminer"
            AppLanguage.SPANISH -> "Finalizar"
            AppLanguage.ITALIAN -> "Completa"
            AppLanguage.ENGLISH -> "Finish"
        }
        else -> when (language) {
            AppLanguage.GREEK -> "Επόμενο"
            AppLanguage.GERMAN -> "Weiter"
            AppLanguage.FRENCH -> "Suivant"
            AppLanguage.SPANISH -> "Siguiente"
            AppLanguage.ITALIAN -> "Avanti"
            AppLanguage.ENGLISH -> "Next"
        }
    }

    val isNextEnabled = currentIndex < totalChapters - 1 || isNextLocked
    val isBackEnabled = currentIndex > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button (Muted Ghost Glass)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isBackEnabled) CosmicVoidSurface
                    else CosmicVoidBg.copy(alpha = 0.40f)
                )
                .border(
                    1.dp,
                    if (isBackEnabled) CosmicBorder
                    else CosmicBorder.copy(alpha = 0.50f),
                    RoundedCornerShape(14.dp)
                )
                .clickable(enabled = isBackEnabled) {
                    AppSoundManager.playTechClick()
                    onPrevious()
                }
                .testTag("btn_prev_chapter"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isBackEnabled) TextPrimaryHighContrast else TextMutedComfort,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = backText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isBackEnabled) TextPrimaryHighContrast else TextMutedComfort
                )
            }
        }

        // Next Button (Solid Amber Glow)
        Box(
            modifier = Modifier
                .weight(1.35f)
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isNextEnabled) {
                        Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                    } else {
                        Brush.horizontalGradient(listOf(CosmicVoidSurface, CosmicVoidBg))
                    }
                )
                .border(
                    1.2.dp,
                    if (isNextEnabled) Color(0xFFF59E0B) else CosmicBorder,
                    RoundedCornerShape(14.dp)
                )
                .clickable(enabled = isNextEnabled) {
                    AppSoundManager.playTechClick()
                    onNext()
                }
                .testTag("btn_next_chapter"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isNextLocked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Pro Locked",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = nextText,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isNextEnabled) Color.Black else TextMutedComfort
                )
                if (currentIndex < totalChapters - 1 && !isNextLocked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = if (isNextEnabled) Color.Black else TextMutedComfort,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterPickerSheetContent(
    chapters: List<BlockchainChapter>,
    currentIndex: Int,
    language: AppLanguage,
    isProUnlocked: Boolean,
    onSelectChapter: (Int) -> Unit,
    onClose: () -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentIndex.coerceAtLeast(0))

    val sheetTitle = when (language) {
        AppLanguage.GREEK -> "Πίνακας Κεφαλαίων"
        AppLanguage.GERMAN -> "Inhaltsverzeichnis"
        AppLanguage.FRENCH -> "Table des matières"
        AppLanguage.SPANISH -> "Índice de Capítulos"
        AppLanguage.ITALIAN -> "Indice dei Capitoli"
        AppLanguage.ENGLISH -> "Table of Contents"
    }

    val sheetSub = when (language) {
        AppLanguage.GREEK -> "17 Κεφάλαια · Blockchain & Πώς Λειτουργεί"
        AppLanguage.GERMAN -> "17 Kapitel · Blockchain & Mechanismen"
        AppLanguage.FRENCH -> "17 Chapitres · Blockchain & Fonctionnement"
        AppLanguage.SPANISH -> "17 Capítulos · Blockchain y Funcionamiento"
        AppLanguage.ITALIAN -> "17 Capitoli · Blockchain e Meccanismi"
        AppLanguage.ENGLISH -> "17 Chapters · Blockchain & Mechanism"
    }

    val freeBadge = when (language) {
        AppLanguage.GREEK -> "ΔΩΡΕΑΝ"
        AppLanguage.GERMAN -> "GRATIS"
        AppLanguage.FRENCH -> "GRATUIT"
        AppLanguage.SPANISH -> "GRATIS"
        AppLanguage.ITALIAN -> "GRATIS"
        AppLanguage.ENGLISH -> "FREE"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = sheetTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryHighContrast
                )
                Text(
                    text = sheetSub,
                    fontSize = 11.5.sp,
                    color = TextSecondaryComfort
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = TextSecondaryComfort
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp)
        ) {
            itemsIndexed(chapters) { index, chapter ->
                val isSelected = index == currentIndex
                val isLocked = chapter.isProOnly && !isProUnlocked

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isSelected -> CopperAccent.copy(alpha = 0.14f)
                                isLocked -> LearnBg.copy(alpha = 0.7f)
                                else -> LearnBg
                            }
                        )
                        .border(
                            1.dp,
                            if (isSelected) CopperAccent else LearnCardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectChapter(index) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> CopperAccent
                                        isLocked -> LearnCardBorder.copy(alpha = 0.6f)
                                        else -> LearnCardBorder
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${chapter.id}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else TextSecondaryComfort
                            )
                        }

                        Text(
                            text = chapter.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                isSelected -> CopperAccent
                                isLocked -> TextSecondaryComfort.copy(alpha = 0.8f)
                                else -> TextPrimaryHighContrast
                            },
                            maxLines = 1
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!chapter.isProOnly) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CopperAccent.copy(alpha = 0.15f))
                                    .border(1.dp, CopperAccent.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = freeBadge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CopperAccent
                                )
                            }
                        } else if (isLocked) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LearnCardBorder)
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Pro",
                                    tint = CopperAccent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "PRO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CopperAccent
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Current",
                                tint = CopperAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
