package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.BlockchainDiagramType
import com.example.ui.theme.CopperAccent
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.SoftCrimson

// Ergonomic diagram palette (Warm slate & gentle accents, low glare)
private val DiagramSurface = Color(0xFF141820)
private val DiagramSurfaceElevated = Color(0xFF1B222D)
private val DiagramBorder = Color(0xFF283242)
private val DiagramTextPrimary = Color(0xFFECEEF2)
private val DiagramTextMuted = Color(0xFF94A3B8)

private val HologramCyan = Color(0xFF38BDF8)
private val HologramAmber = Color(0xFFF59E0B)
private val HologramCrimson = Color(0xFFF87171)
private val HologramMint = Color(0xFF34D399)
private val HologramMauve = Color(0xFFA78BFA)

@Composable
fun ChapterDiagram(
    diagramType: BlockchainDiagramType,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DiagramSurface)
            .border(
                1.2.dp,
                DiagramBorder,
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        when (diagramType) {
            BlockchainDiagramType.DOUBLE_SPENDING -> DoubleSpendingDiagram(language)
            BlockchainDiagramType.CENTRALIZED_VS_DISTRIBUTED -> CentralizedVsDistributedDiagram(language)
            BlockchainDiagramType.CRYPTOGRAPHIC_HASH -> CryptographicHashDiagram(language)
            BlockchainDiagramType.KEYS_AND_SIGNATURES -> KeysAndSignaturesDiagram(language)
            BlockchainDiagramType.TRANSACTION_FIELDS -> TransactionFieldsDiagram(language)
            BlockchainDiagramType.UTXO_VS_ACCOUNT -> UtxoVsAccountDiagram(language)
            BlockchainDiagramType.BLOCK_ANATOMY -> BlockAnatomyDiagram(language)
            BlockchainDiagramType.BLOCK_CHAINING -> BlockChainingDiagram(language)
            BlockchainDiagramType.P2P_NODES -> P2pNodesDiagram(language)
            BlockchainDiagramType.CONSENSUS_FLOW -> ConsensusFlowDiagram(language)
            BlockchainDiagramType.PROOF_OF_WORK -> ProofOfWorkDiagram(language)
            BlockchainDiagramType.PROOF_OF_STAKE -> ProofOfStakeDiagram(language)
            BlockchainDiagramType.MEMPOOL_FEES -> MempoolFeesDiagram(language)
            BlockchainDiagramType.WALLET_SEED_HIERARCHY -> WalletSeedHierarchyDiagram(language)
            BlockchainDiagramType.COIN_VS_TOKEN -> CoinVsTokenDiagram(language)
            BlockchainDiagramType.SMART_CONTRACT_STATE -> SmartContractStateDiagram(language)
            BlockchainDiagramType.SYSTEM_LIMITS -> SystemLimitsDiagram(language)
            BlockchainDiagramType.LIQUIDATION_ENGINEERING -> LiquidationMechanicsDiagram(language)
            BlockchainDiagramType.FUNDING_DYNAMICS -> FundingMechanicsDiagram(language)
            BlockchainDiagramType.QUANTUM_ORDER_FLOW -> OrderBookDiagram(language)
            BlockchainDiagramType.MACRO_HALVING_CYCLES -> HalvingIssuanceDiagram(language)
            BlockchainDiagramType.INSTITUTIONAL_RISK -> VenueLossCoverDiagram(language)
        }
    }
}

@Composable
private fun GeoBox(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    highlight: Boolean = false,
    small: Boolean = false
) {
    val borderCol = if (highlight) HologramAmber.copy(alpha = 0.8f) else DiagramBorder
    val bgCol = if (highlight) {
        HologramAmber.copy(alpha = 0.12f)
    } else {
        DiagramSurfaceElevated
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgCol)
            .border(
                1.dp,
                borderCol,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = if (small) 10.dp else 14.dp, vertical = if (small) 8.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = if (small) 11.5.sp else 13.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (highlight) HologramAmber else DiagramTextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                fontSize = if (small) 9.5.sp else 10.5.sp,
                color = DiagramTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}

// 1. DOUBLE SPENDING (Floating Holographic Memory Nodes with Animated Photon Data Stream)
@Composable
private fun DoubleSpendingDiagram(language: AppLanguage) {
    val fileTitle = when (language) {
        AppLanguage.GREEK -> "1 Ψηφιακό\nΑρχείο"
        AppLanguage.GERMAN -> "1 Digitale\nDatei"
        AppLanguage.FRENCH -> "1 Fichier\nNumérique"
        AppLanguage.SPANISH -> "1 Archivo\nDigital"
        AppLanguage.ITALIAN -> "1 File\nDigitale"
        AppLanguage.ENGLISH -> "1 Digital\nFile"
    }
    val fileSub = when (language) {
        AppLanguage.GREEK -> "αρχικό αντίτυπο"
        AppLanguage.GERMAN -> "Originaldatei"
        AppLanguage.FRENCH -> "original"
        AppLanguage.SPANISH -> "original"
        AppLanguage.ITALIAN -> "originale"
        AppLanguage.ENGLISH -> "original asset"
    }
    val copyText = when (language) {
        AppLanguage.GREEK -> "εύκολο copy"
        AppLanguage.GERMAN -> "leicht kopiert"
        AppLanguage.FRENCH -> "copie facile"
        AppLanguage.SPANISH -> "copia fácil"
        AppLanguage.ITALIAN -> "copia facile"
        AppLanguage.ENGLISH -> "easy copy"
    }
    val copyATitle = when (language) {
        AppLanguage.GREEK -> "Αντίγραφο Α"
        AppLanguage.GERMAN -> "Kopie A"
        AppLanguage.FRENCH -> "Copie A"
        AppLanguage.SPANISH -> "Copia A"
        AppLanguage.ITALIAN -> "Copia A"
        AppLanguage.ENGLISH -> "Copy A"
    }
    val copyASub = when (language) {
        AppLanguage.GREEK -> "ξόδεμα στον 1ο"
        AppLanguage.GERMAN -> "Ausgabe an 1."
        AppLanguage.FRENCH -> "dépense à 1er"
        AppLanguage.SPANISH -> "gasto al 1º"
        AppLanguage.ITALIAN -> "spesa al 1°"
        AppLanguage.ENGLISH -> "spent to 1st"
    }
    val copyBTitle = when (language) {
        AppLanguage.GREEK -> "Αντίγραφο Β"
        AppLanguage.GERMAN -> "Kopie B"
        AppLanguage.FRENCH -> "Copie B"
        AppLanguage.SPANISH -> "Copia B"
        AppLanguage.ITALIAN -> "Copia B"
        AppLanguage.ENGLISH -> "Copy B"
    }
    val copyBSub = when (language) {
        AppLanguage.GREEK -> "ξόδεμα στον 2ο"
        AppLanguage.GERMAN -> "Ausgabe an 2."
        AppLanguage.FRENCH -> "dépense à 2e"
        AppLanguage.SPANISH -> "gasto al 2º"
        AppLanguage.ITALIAN -> "spesa al 2°"
        AppLanguage.ENGLISH -> "spent to 2nd"
    }
    val footer = when (language) {
        AppLanguage.GREEK -> "Χωρίς κοινό κανόνα -> ο ίδιος άνθρωπος θα το έστελνε δύο φορές"
        AppLanguage.GERMAN -> "Ohne Konsens -> dieselbe Person könnte doppelt ausgeben"
        AppLanguage.FRENCH -> "Sans consensus -> la même personne pourrait dépenser deux fois"
        AppLanguage.SPANISH -> "Sin consenso común -> el mismo emisor podría gastarlo dos veces"
        AppLanguage.ITALIAN -> "Senza consenso comune -> la stessa persona potrebbe spenderlo due volte"
        AppLanguage.ENGLISH -> "Without shared consensus -> same user could send it twice"
    }

    // Infinite transition for traveling photon packets and glowing beam pulsation
    val infiniteTransition = rememberInfiniteTransition(label = "PhotonStream")
    val photonProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "photonProgress"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Diagram Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(HologramMint)
                )
                Text(
                    text = "DIAGRAM // 0x01-A",
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = HologramMint,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = "TOPOLOGY: BIFURCATED",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = HologramCyan,
                letterSpacing = 0.5.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source Node: 1 Digital File (Holographic Memory Node in Quantum Cyan)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DiagramSurfaceElevated)
                    .border(
                        1.2.dp,
                        HologramCyan.copy(alpha = 0.60f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Tech Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(HologramCyan)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SRC_NODE",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = HologramCyan,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = fileTitle,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = fileSub,
                        fontSize = 9.5.sp,
                        color = HologramCyan,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Center Connector: Animated Data Stream with Tiny Glowing Photons flowing left to right
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Animated Photon Laser Stream
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val startX = 0f
                    val startY = h / 2f

                    val topEndX = w
                    val topEndY = h * 0.26f

                    val bottomEndX = w
                    val bottomEndY = h * 0.74f

                    // Path 1 (to Top Copy A)
                    val pathTop = Path().apply {
                        moveTo(startX, startY)
                        cubicTo(
                            startX + w * 0.45f, startY,
                            startX + w * 0.55f, topEndY,
                            topEndX, topEndY
                        )
                    }

                    // Path 2 (to Bottom Copy B)
                    val pathBottom = Path().apply {
                        moveTo(startX, startY)
                        cubicTo(
                            startX + w * 0.45f, startY,
                            startX + w * 0.55f, bottomEndY,
                            bottomEndX, bottomEndY
                        )
                    }

                    // Static subtle laser conduit tracks
                    val conduitColor = Color(0xFF1E2E48)
                    drawPath(pathTop, color = conduitColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                    drawPath(pathBottom, color = conduitColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

                    // Pulsing luminous conduit overlay
                    val streamBrushTop = Brush.horizontalGradient(
                        listOf(HologramCyan.copy(alpha = 0.8f), HologramCyan.copy(alpha = 0.5f))
                    )
                    val streamBrushBottom = Brush.horizontalGradient(
                        listOf(HologramMauve.copy(alpha = 0.8f), HologramMauve.copy(alpha = 0.5f))
                    )

                    drawPath(
                        pathTop,
                        brush = streamBrushTop,
                        style = Stroke(
                            width = 1.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    )
                    drawPath(
                        pathBottom,
                        brush = streamBrushBottom,
                        style = Stroke(
                            width = 1.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    )

                    // Draw 3 traveling photon particles along Top Path
                    val pOffsets = floatArrayOf(0f, 0.33f, 0.66f)
                    pOffsets.forEach { offsetFrac ->
                        val p = (photonProgress + offsetFrac) % 1f
                        val t = p
                        val oneMinusT = 1f - t
                        val p0 = Offset(startX, startY)
                        val p1 = Offset(startX + w * 0.45f, startY)
                        val p2 = Offset(startX + w * 0.55f, topEndY)
                        val p3 = Offset(topEndX, topEndY)

                        val px = (oneMinusT * oneMinusT * oneMinusT * p0.x) +
                                (3f * oneMinusT * oneMinusT * t * p1.x) +
                                (3f * oneMinusT * t * t * p2.x) +
                                (t * t * t * p3.x)
                        val py = (oneMinusT * oneMinusT * oneMinusT * p0.y) +
                                (3f * oneMinusT * oneMinusT * t * p1.y) +
                                (3f * oneMinusT * t * t * p2.y) +
                                (t * t * t * p3.y)

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(HologramCyan.copy(alpha = 0.7f), Color.Transparent),
                                center = Offset(px, py),
                                radius = 7.dp.toPx()
                            ),
                            radius = 7.dp.toPx(),
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }

                    // Draw 3 traveling photon particles along Bottom Path
                    pOffsets.forEach { offsetFrac ->
                        val p = (photonProgress + offsetFrac) % 1f
                        val t = p
                        val oneMinusT = 1f - t
                        val p0 = Offset(startX, startY)
                        val p1 = Offset(startX + w * 0.45f, startY)
                        val p2 = Offset(startX + w * 0.55f, bottomEndY)
                        val p3 = Offset(bottomEndX, bottomEndY)

                        val px = (oneMinusT * oneMinusT * oneMinusT * p0.x) +
                                (3f * oneMinusT * oneMinusT * t * p1.x) +
                                (3f * oneMinusT * t * t * p2.x) +
                                (t * t * t * p3.x)
                        val py = (oneMinusT * oneMinusT * oneMinusT * p0.y) +
                                (3f * oneMinusT * oneMinusT * t * p1.y) +
                                (3f * oneMinusT * t * t * p2.y) +
                                (t * t * t * p3.y)

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(HologramMauve.copy(alpha = 0.7f), Color.Transparent),
                                center = Offset(px, py),
                                radius = 7.dp.toPx()
                            ),
                            radius = 7.dp.toPx(),
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }

                // Central floating tag badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DiagramSurfaceElevated)
                        .border(1.dp, HologramAmber.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = copyText,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = HologramAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Destination Nodes: Copy A & Copy B (Holographic Memory Nodes)
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .height(140.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Copy A
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DiagramSurfaceElevated)
                        .border(
                            1.dp,
                            HologramCyan.copy(alpha = 0.6f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(HologramCyan)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = copyATitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = copyASub,
                            fontSize = 9.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Copy B
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DiagramSurfaceElevated)
                        .border(
                            1.dp,
                            HologramMauve.copy(alpha = 0.6f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(HologramMauve)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = copyBTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = copyBSub,
                            fontSize = 9.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Warning Footer Callout (Crimson Warning Box)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(HologramCrimson.copy(alpha = 0.12f))
                .border(0.8.dp, HologramCrimson.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = HologramCrimson,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = footer,
                    fontSize = 10.5.sp,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// 2. CENTRALIZED VS DISTRIBUTED
@Composable
private fun CentralizedVsDistributedDiagram(language: AppLanguage) {
    val centralTitle = when (language) {
        AppLanguage.GREEK -> "Κεντρικός Server"
        AppLanguage.GERMAN -> "Zentraler Server"
        AppLanguage.FRENCH -> "Serveur Central"
        AppLanguage.SPANISH -> "Servidor Central"
        AppLanguage.ITALIAN -> "Server Centrale"
        AppLanguage.ENGLISH -> "Central Server"
    }
    val centralSub = when (language) {
        AppLanguage.GREEK -> "1 φορέας ελέγχει\nτο επίσημο βιβλίο"
        AppLanguage.GERMAN -> "1 Instanz führt\ndas Hauptbuch"
        AppLanguage.FRENCH -> "1 entité gère\nle registre"
        AppLanguage.SPANISH -> "1 entidad controla\nel registro oficial"
        AppLanguage.ITALIAN -> "1 ente gestisce\nil registro"
        AppLanguage.ENGLISH -> "1 entity controls\nthe ledger"
    }
    val nodesText = when (language) {
        AppLanguage.GREEK -> "Ίδιο αντίγραφο σε όλους"
        AppLanguage.GERMAN -> "Gleiche Kopie auf allen Nodes"
        AppLanguage.FRENCH -> "Même copie sur chaque nœud"
        AppLanguage.SPANISH -> "Misma copia en cada nodo"
        AppLanguage.ITALIAN -> "Stessa copia su tutti i nodi"
        AppLanguage.ENGLISH -> "Same copy on all nodes"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GeoBox(
            title = centralTitle,
            subtitle = centralSub,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "vs",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = CopperAccent
        )

        Column(
            modifier = Modifier
                .weight(1.2f)
                .clip(RoundedCornerShape(10.dp))
                .background(DiagramSurfaceElevated)
                .border(1.dp, CopperAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GeoBox(title = "Node 1", small = true, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(6.dp))
                GeoBox(title = "Node 2", small = true, modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GeoBox(title = "Node 3", small = true, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(6.dp))
                GeoBox(title = "Node 4", small = true, modifier = Modifier.weight(1f))
            }
            Text(
                text = nodesText,
                fontSize = 10.sp,
                color = CopperAccent,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// 3. CRYPTOGRAPHIC HASH
@Composable
private fun CryptographicHashDiagram(language: AppLanguage) {
    val inputTitle = when (language) {
        AppLanguage.GREEK -> "Είσοδος"
        AppLanguage.GERMAN -> "Eingabedaten"
        AppLanguage.FRENCH -> "Données d'entrée"
        AppLanguage.SPANISH -> "Datos de entrada"
        AppLanguage.ITALIAN -> "Dati di input"
        AppLanguage.ENGLISH -> "Input Data"
    }
    val inputSub = when (language) {
        AppLanguage.GREEK -> "οποιοδήποτε κείμενο\nή συναλλαγή"
        AppLanguage.GERMAN -> "beliebige Nachricht\noder Transaktion"
        AppLanguage.FRENCH -> "taille quelconque\nou transaction"
        AppLanguage.SPANISH -> "cualquier tamaño\no transacción"
        AppLanguage.ITALIAN -> "qualsiasi dimensione\no transazione"
        AppLanguage.ENGLISH -> "any message size\nor transaction"
    }
    val hashSub = when (language) {
        AppLanguage.GREEK -> "64 δεκαεξαδικοί\nχαρακτήρες"
        AppLanguage.GERMAN -> "64 Hex-Zeichen\n(32 Bytes)"
        AppLanguage.FRENCH -> "64 caractères hex\n(32 octets)"
        AppLanguage.SPANISH -> "64 caracteres hex\n(32 bytes)"
        AppLanguage.ITALIAN -> "64 caratteri esad.\n(32 byte)"
        AppLanguage.ENGLISH -> "64 hex chars\n(32 bytes)"
    }
    val footer = when (language) {
        AppLanguage.GREEK -> "a7b9c... 32 bytes σταθερού μήκους · Μη αναστρέψιμο"
        AppLanguage.GERMAN -> "a7b9c... 32 Bytes feste Länge · Nicht umkehrbar"
        AppLanguage.FRENCH -> "a7b9c... 32 octets longueur fixe · Non réversible"
        AppLanguage.SPANISH -> "a7b9c... 32 bytes longitud fija · No invertible"
        AppLanguage.ITALIAN -> "a7b9c... 32 byte lunghezza fissa · Non invertibile"
        AppLanguage.ENGLISH -> "a7b9c... 32 bytes fixed length · One-way digest"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GeoBox(
                title = inputTitle,
                subtitle = inputSub,
                modifier = Modifier.weight(1f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Text(
                    text = "SHA-256",
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CopperAccent
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = CopperAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            GeoBox(
                title = "Hash (Digest)",
                subtitle = hashSub,
                highlight = true,
                modifier = Modifier.weight(1.1f)
            )
        }

        Text(
            text = footer,
            fontSize = 10.5.sp,
            fontFamily = FontFamily.Monospace,
            color = DiagramTextMuted
        )
    }
}

// 4. KEYS AND SIGNATURES
@Composable
private fun KeysAndSignaturesDiagram(language: AppLanguage) {
    val privTitle = when (language) {
        AppLanguage.GREEK -> "Ιδιωτικό Κλειδί"
        AppLanguage.GERMAN -> "Privater Schlüssel"
        AppLanguage.FRENCH -> "Clé Privée"
        AppLanguage.SPANISH -> "Clave Privada"
        AppLanguage.ITALIAN -> "Chiave Privata"
        AppLanguage.ENGLISH -> "Private Key"
    }
    val privSub = when (language) {
        AppLanguage.GREEK -> "ΜΕΝΕΙ ΜΥΣΤΙΚΟ"
        AppLanguage.GERMAN -> "BLEIBT GEHEIM"
        AppLanguage.FRENCH -> "RESTE SECRÈTE"
        AppLanguage.SPANISH -> "SECRETO TOTAL"
        AppLanguage.ITALIAN -> "RIMANE SEGRETA"
        AppLanguage.ENGLISH -> "STAYS SECRET"
    }
    val signAction = when (language) {
        AppLanguage.GREEK -> "υπογράφει"
        AppLanguage.GERMAN -> "signiert"
        AppLanguage.FRENCH -> "signe"
        AppLanguage.SPANISH -> "firma"
        AppLanguage.ITALIAN -> "firma"
        AppLanguage.ENGLISH -> "signs"
    }
    val sigTitle = when (language) {
        AppLanguage.GREEK -> "Ψηφιακή Υπογραφή"
        AppLanguage.GERMAN -> "Digitale Signatur"
        AppLanguage.FRENCH -> "Signature Numérique"
        AppLanguage.SPANISH -> "Firma Digital"
        AppLanguage.ITALIAN -> "Firma Digitale"
        AppLanguage.ENGLISH -> "Digital Signature"
    }
    val sigSub = when (language) {
        AppLanguage.GREEK -> "απόδειξη κατοχής"
        AppLanguage.GERMAN -> "Besitznachweis"
        AppLanguage.FRENCH -> "preuve d'autorité"
        AppLanguage.SPANISH -> "prueba de autoría"
        AppLanguage.ITALIAN -> "prova di possesso"
        AppLanguage.ENGLISH -> "proof of ownership"
    }
    val pubTitle = when (language) {
        AppLanguage.GREEK -> "Δημόσιο Κλειδί / Διεύθυνση"
        AppLanguage.GERMAN -> "Öffentlicher Schlüssel / Adresse"
        AppLanguage.FRENCH -> "Clé Publique / Adresse"
        AppLanguage.SPANISH -> "Clave Pública / Dirección"
        AppLanguage.ITALIAN -> "Chiave Pubblica / Indirizzo"
        AppLanguage.ENGLISH -> "Public Key / Address"
    }
    val pubSub = when (language) {
        AppLanguage.GREEK -> "Κάθε node επαληθεύει δημόσια χωρίς να δει το μυστικό"
        AppLanguage.GERMAN -> "Jeder Node prüft öffentlich, ohne den privaten Schlüssel zu kennen"
        AppLanguage.FRENCH -> "Chaque nœud vérifie publiquement sans connaître le secret"
        AppLanguage.SPANISH -> "Cualquier nodo verifica públicamente sin conocer el secreto"
        AppLanguage.ITALIAN -> "Ogni nodo verifica pubblicamente senza conoscere il segreto"
        AppLanguage.ENGLISH -> "Any node verifies publicly without knowing the secret"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GeoBox(
                title = privTitle,
                subtitle = privSub,
                highlight = true,
                modifier = Modifier.weight(1f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Text(
                    text = signAction,
                    fontSize = 11.sp,
                    color = CopperAccent
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = CopperAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            GeoBox(
                title = sigTitle,
                subtitle = sigSub,
                modifier = Modifier.weight(1.1f)
            )
        }

        GeoBox(
            title = pubTitle,
            subtitle = pubSub,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 5. TRANSACTION FIELDS
@Composable
private fun TransactionFieldsDiagram(language: AppLanguage) {
    val fromLabel = when (language) {
        AppLanguage.GREEK -> "Από (Inputs)"
        AppLanguage.GERMAN -> "Von (Inputs)"
        AppLanguage.FRENCH -> "De (Inputs)"
        AppLanguage.SPANISH -> "Desde (Inputs)"
        AppLanguage.ITALIAN -> "Da (Inputs)"
        AppLanguage.ENGLISH -> "From (Inputs)"
    }
    val toLabel = when (language) {
        AppLanguage.GREEK -> "Προς (Outputs)"
        AppLanguage.GERMAN -> "An (Outputs)"
        AppLanguage.FRENCH -> "Vers (Outputs)"
        AppLanguage.SPANISH -> "Hacia (Outputs)"
        AppLanguage.ITALIAN -> "A (Outputs)"
        AppLanguage.ENGLISH -> "To (Outputs)"
    }
    val sigLabel = when (language) {
        AppLanguage.GREEK -> "Υπογραφή (Signature)"
        AppLanguage.GERMAN -> "Signatur (Signature)"
        AppLanguage.FRENCH -> "Signature (Secp256k1)"
        AppLanguage.SPANISH -> "Firma (Signature)"
        AppLanguage.ITALIAN -> "Firma (Signature)"
        AppLanguage.ENGLISH -> "Signature"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GeoBox(
                title = fromLabel,
                subtitle = "0x4a8... (UTXOs)",
                modifier = Modifier.weight(1f),
                small = true
            )
            GeoBox(
                title = toLabel,
                subtitle = "0x9e1... (0.50 BTC)",
                modifier = Modifier.weight(1f),
                small = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GeoBox(
                title = sigLabel,
                subtitle = "ecdsa_sig_8f...",
                highlight = true,
                modifier = Modifier.weight(1f),
                small = true
            )
            GeoBox(
                title = "TXID",
                subtitle = "Hash(Data)",
                modifier = Modifier.weight(1f),
                small = true
            )
        }
    }
}

// 6. UTXO VS ACCOUNT
@Composable
private fun UtxoVsAccountDiagram(language: AppLanguage) {
    val utxoSub = when (language) {
        AppLanguage.GREEK -> "Σύνολο κερμάτων/εξόδων\nΚάθε tx καταναλώνει UTXO"
        AppLanguage.GERMAN -> "Menge unspent Ausgänge\nTx verbraucht ganzes UTXO"
        AppLanguage.FRENCH -> "Sorties non dépensées\nTx consomme des UTXO"
        AppLanguage.SPANISH -> "Salidas no gastadas\nTx consume UTXO completo"
        AppLanguage.ITALIAN -> "Output non spesi\nTx consuma interi UTXO"
        AppLanguage.ENGLISH -> "Unspent outputs\nTx consumes whole UTXO"
    }
    val accSub = when (language) {
        AppLanguage.GREEK -> "Υπόλοιπο διεύθυνσης\nΚάθε tx αφαιρεί/προσθέτει"
        AppLanguage.GERMAN -> "Globaler Kontozustand\nTx ändert Saldo direkt"
        AppLanguage.FRENCH -> "État global du compte\nTx met à jour le solde"
        AppLanguage.SPANISH -> "Estado de cuenta global\nTx modifica el saldo"
        AppLanguage.ITALIAN -> "Stato account globale\nTx aggiorna il saldo"
        AppLanguage.ENGLISH -> "Global account state\nTx mutates balance"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GeoBox(
            title = "UTXO (Bitcoin)",
            subtitle = utxoSub,
            modifier = Modifier.weight(1f),
            highlight = true
        )
        GeoBox(
            title = "Account (Ethereum)",
            subtitle = accSub,
            modifier = Modifier.weight(1f)
        )
    }
}

// 7. BLOCK ANATOMY
@Composable
private fun BlockAnatomyDiagram(language: AppLanguage) {
    val headerTitle = when (language) {
        AppLanguage.GREEK -> "BLOCK HEADER (Επικεφαλίδα)"
        AppLanguage.GERMAN -> "BLOCK HEADER (Blockkopf)"
        AppLanguage.FRENCH -> "EN-TÊTE DU BLOC (Header)"
        AppLanguage.SPANISH -> "CABECERA DE BLOQUE (Header)"
        AppLanguage.ITALIAN -> "INTESTAZIONE BLOCCO (Header)"
        AppLanguage.ENGLISH -> "BLOCK HEADER"
    }
    val bodyTitle = when (language) {
        AppLanguage.GREEK -> "ΣΩΜΑ ΣΥΝΑΛΛΑΓΩΝ (Body)"
        AppLanguage.GERMAN -> "TRANSAKTIONS-BODY (Körper)"
        AppLanguage.FRENCH -> "CORPS DES TRANSACTIONS"
        AppLanguage.SPANISH -> "CUERPO DE TRANSACCIONES"
        AppLanguage.ITALIAN -> "CORPO DELLE TRANSAZIONI"
        AppLanguage.ENGLISH -> "TRANSACTION BODY"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GeoBox(
            title = headerTitle,
            subtitle = "Prev Hash · Merkle Root · Timestamp · Nonce",
            highlight = true,
            modifier = Modifier.fillMaxWidth()
        )
        GeoBox(
            title = bodyTitle,
            subtitle = "Tx 1 · Tx 2 · Tx 3 · ... · Tx N (Merkle Tree)",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 8. BLOCK CHAINING
@Composable
private fun BlockChainingDiagram(language: AppLanguage) {
    val footer = when (language) {
        AppLanguage.GREEK -> "Αλλαγή στο #100 αλλάζει το hash του & σπάει την αλυσίδα"
        AppLanguage.GERMAN -> "Änderung an #100 ändert den Hash & bricht die Kette"
        AppLanguage.FRENCH -> "Modifier #100 change son hash & brise toute la chaîne"
        AppLanguage.SPANISH -> "Modificar #100 cambia su hash y rompe la cadena posterior"
        AppLanguage.ITALIAN -> "Modificare #100 cambia il suo hash e spezza la catena"
        AppLanguage.ENGLISH -> "Altering #100 changes its hash & breaks downstream chain"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GeoBox(title = "Block 100", subtitle = "hash: ...9a", modifier = Modifier.weight(1f), small = true)

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = CopperAccent,
                modifier = Modifier.size(16.dp)
            )

            GeoBox(title = "Block 101", subtitle = "prev: ...9a", modifier = Modifier.weight(1f), small = true)

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = CopperAccent,
                modifier = Modifier.size(16.dp)
            )

            GeoBox(title = "Block 102", subtitle = "prev: ...4b", highlight = true, modifier = Modifier.weight(1f), small = true)
        }

        Text(
            text = footer,
            fontSize = 11.sp,
            color = DiagramTextMuted,
            textAlign = TextAlign.Center
        )
    }
}

// 9. P2P NODES
@Composable
private fun P2pNodesDiagram(language: AppLanguage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.75f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GeoBox(title = "Node A", small = true, modifier = Modifier.width(70.dp))
            GeoBox(title = "Node B", small = true, modifier = Modifier.width(70.dp))
        }

        Text(
            text = "⤮ peer-to-peer ⤯",
            fontSize = 12.sp,
            color = CopperAccent,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GeoBox(title = "Node C", small = true, modifier = Modifier.width(70.dp))
            GeoBox(title = "Node D", highlight = true, small = true, modifier = Modifier.width(70.dp))
            GeoBox(title = "Node E", small = true, modifier = Modifier.width(70.dp))
        }
    }
}

// 10. CONSENSUS FLOW
@Composable
private fun ConsensusFlowDiagram(language: AppLanguage) {
    val step1Title = when (language) {
        AppLanguage.GREEK -> "1. Πρόταση"
        AppLanguage.GERMAN -> "1. Vorschlag"
        AppLanguage.FRENCH -> "1. Proposition"
        AppLanguage.SPANISH -> "1. Propuesta"
        AppLanguage.ITALIAN -> "1. Proposta"
        AppLanguage.ENGLISH -> "1. Proposal"
    }
    val step1Sub = when (language) {
        AppLanguage.GREEK -> "νέο block"
        AppLanguage.GERMAN -> "neuer Block"
        AppLanguage.FRENCH -> "nouveau bloc"
        AppLanguage.SPANISH -> "nuevo bloque"
        AppLanguage.ITALIAN -> "nuovo blocco"
        AppLanguage.ENGLISH -> "new block"
    }
    val step2Title = when (language) {
        AppLanguage.GREEK -> "2. Κανόνας"
        AppLanguage.GERMAN -> "2. Konsens"
        AppLanguage.FRENCH -> "2. Consensus"
        AppLanguage.SPANISH -> "2. Consenso"
        AppLanguage.ITALIAN -> "2. Consenso"
        AppLanguage.ENGLISH -> "2. Consensus"
    }
    val step2Sub = when (language) {
        AppLanguage.GREEK -> "έλεγχος node"
        AppLanguage.GERMAN -> "Regelprüfung"
        AppLanguage.FRENCH -> "validation"
        AppLanguage.SPANISH -> "validación"
        AppLanguage.ITALIAN -> "regola nodi"
        AppLanguage.ENGLISH -> "validity rule"
    }
    val step3Title = when (language) {
        AppLanguage.GREEK -> "3. Αλυσίδα"
        AppLanguage.GERMAN -> "3. Kette"
        AppLanguage.FRENCH -> "3. Chaîne"
        AppLanguage.SPANISH -> "3. Cadena"
        AppLanguage.ITALIAN -> "3. Catena"
        AppLanguage.ENGLISH -> "3. Chain"
    }
    val step3Sub = when (language) {
        AppLanguage.GREEK -> "1 κοινή ιστορία"
        AppLanguage.GERMAN -> "1 Historie"
        AppLanguage.FRENCH -> "1 histoire"
        AppLanguage.SPANISH -> "1 historial"
        AppLanguage.ITALIAN -> "1 verità"
        AppLanguage.ENGLISH -> "1 truth"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GeoBox(
            title = step1Title,
            subtitle = step1Sub,
            modifier = Modifier.weight(1f),
            small = true
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = CopperAccent,
            modifier = Modifier.size(16.dp)
        )

        GeoBox(
            title = step2Title,
            subtitle = step2Sub,
            highlight = true,
            modifier = Modifier.weight(1.1f),
            small = true
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = CopperAccent,
            modifier = Modifier.size(16.dp)
        )

        GeoBox(
            title = step3Title,
            subtitle = step3Sub,
            modifier = Modifier.weight(1f),
            small = true
        )
    }
}

// 11. PROOF OF WORK
@Composable
private fun ProofOfWorkDiagram(language: AppLanguage) {
    val noncesSub = when (language) {
        AppLanguage.GREEK -> "δοκιμή τιμών"
        AppLanguage.GERMAN -> "Nonce testen"
        AppLanguage.FRENCH -> "tester nonces"
        AppLanguage.SPANISH -> "probar nonce"
        AppLanguage.ITALIAN -> "testa nonce"
        AppLanguage.ENGLISH -> "try nonces"
    }
    val targetTitle = when (language) {
        AppLanguage.GREEK -> "Στόχος Target;"
        AppLanguage.GERMAN -> "Difficulty-Ziel?"
        AppLanguage.FRENCH -> "Cible Target ?"
        AppLanguage.SPANISH -> "¿Objetivo Target?"
        AppLanguage.ITALIAN -> "Target Verifica?"
        AppLanguage.ENGLISH -> "Target Check"
    }
    val targetSub = when (language) {
        AppLanguage.GREEK -> "< target -> OK\nαλλιώς νέο nonce"
        AppLanguage.GERMAN -> "< Target -> OK\nsonst neues Nonce"
        AppLanguage.FRENCH -> "< cible -> OK\nsinon nouveau nonce"
        AppLanguage.SPANISH -> "< target -> OK\nsi no, nuevo nonce"
        AppLanguage.ITALIAN -> "< target -> OK\naltrimenti nuovo"
        AppLanguage.ENGLISH -> "< target -> OK\nelse new nonce"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GeoBox(
                title = "Header + Nonce",
                subtitle = noncesSub,
                modifier = Modifier.weight(1f),
                small = true
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = CopperAccent,
                modifier = Modifier.size(16.dp)
            )

            GeoBox(
                title = "SHA-256",
                subtitle = "Hash",
                highlight = true,
                modifier = Modifier.weight(0.9f),
                small = true
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = CopperAccent,
                modifier = Modifier.size(16.dp)
            )

            GeoBox(
                title = targetTitle,
                subtitle = targetSub,
                modifier = Modifier.weight(1.2f),
                small = true
            )
        }
    }
}

// 12. PROOF OF STAKE
@Composable
private fun ProofOfStakeDiagram(language: AppLanguage) {
    val stakeSub = when (language) {
        AppLanguage.GREEK -> "δέσμευση κεφαλαίου"
        AppLanguage.GERMAN -> "Kapital sperren"
        AppLanguage.FRENCH -> "capital verrouillé"
        AppLanguage.SPANISH -> "capital bloqueado"
        AppLanguage.ITALIAN -> "capitale vincolato"
        AppLanguage.ENGLISH -> "locked capital"
    }
    val propSub = when (language) {
        AppLanguage.GREEK -> "τυχαία επιλογή"
        AppLanguage.GERMAN -> "Zufallsauswahl"
        AppLanguage.FRENCH -> "sélection aléatoire"
        AppLanguage.SPANISH -> "selección aleatoria"
        AppLanguage.ITALIAN -> "selezione casuale"
        AppLanguage.ENGLISH -> "pseudo-random"
    }
    val attestSub = when (language) {
        AppLanguage.GREEK -> "επικύρωση / slash"
        AppLanguage.GERMAN -> "Bestätigung / Slash"
        AppLanguage.FRENCH -> "attestation / slash"
        AppLanguage.SPANISH -> "atestación / slash"
        AppLanguage.ITALIAN -> "attestazione / slash"
        AppLanguage.ENGLISH -> "attestation / slash"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GeoBox(
            title = "1. Stake",
            subtitle = stakeSub,
            modifier = Modifier.weight(1f),
            small = true
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = CopperAccent,
            modifier = Modifier.size(14.dp)
        )

        GeoBox(
            title = "2. Proposer",
            subtitle = propSub,
            highlight = true,
            modifier = Modifier.weight(1.1f),
            small = true
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = CopperAccent,
            modifier = Modifier.size(14.dp)
        )

        GeoBox(
            title = "3. Attest",
            subtitle = attestSub,
            modifier = Modifier.weight(1f),
            small = true
        )
    }
}

// 13. MEMPOOL FEES
@Composable
private fun MempoolFeesDiagram(language: AppLanguage) {
    val mempoolSub = when (language) {
        AppLanguage.GREEK -> "ουρά εκκρεμών\nσυναλλαγών"
        AppLanguage.GERMAN -> "Warteschlange\nunbestätigter Txs"
        AppLanguage.FRENCH -> "file d'attente\ntransactions"
        AppLanguage.SPANISH -> "cola de espera\ntransacciones"
        AppLanguage.ITALIAN -> "coda transazioni\nin attesa"
        AppLanguage.ENGLISH -> "unconfirmed txs\npending pool"
    }
    val priorityText = when (language) {
        AppLanguage.GREEK -> "προτεραιότητα fee"
        AppLanguage.GERMAN -> "Gebühren-Priorität"
        AppLanguage.FRENCH -> "priorité aux frais"
        AppLanguage.SPANISH -> "prioridad por tarifa"
        AppLanguage.ITALIAN -> "priorità tariffa"
        AppLanguage.ENGLISH -> "priority by fee"
    }
    val nextBlockTitle = when (language) {
        AppLanguage.GREEK -> "Επόμενο Block"
        AppLanguage.GERMAN -> "Nächster Block"
        AppLanguage.FRENCH -> "Prochain Bloc"
        AppLanguage.SPANISH -> "Siguiente Bloque"
        AppLanguage.ITALIAN -> "Prossimo Blocco"
        AppLanguage.ENGLISH -> "Next Block"
    }
    val nextBlockSub = when (language) {
        AppLanguage.GREEK -> "περιορισμένος χώρος"
        AppLanguage.GERMAN -> "knapper Blockplatz"
        AppLanguage.FRENCH -> "espace limité"
        AppLanguage.SPANISH -> "espacio limitado"
        AppLanguage.ITALIAN -> "spazio limitato"
        AppLanguage.ENGLISH -> "scarce block space"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GeoBox(
            title = "Mempool",
            subtitle = mempoolSub,
            modifier = Modifier.weight(1.1f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = priorityText,
                fontSize = 10.sp,
                color = CopperAccent
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = CopperAccent,
                modifier = Modifier.size(16.dp)
            )
        }

        GeoBox(
            title = nextBlockTitle,
            subtitle = nextBlockSub,
            highlight = true,
            modifier = Modifier.weight(1.1f)
        )
    }
}

// 14. WALLET SEED HIERARCHY
@Composable
private fun WalletSeedHierarchyDiagram(language: AppLanguage) {
    val seedSub = when (language) {
        AppLanguage.GREEK -> "12/24 λέξεις"
        AppLanguage.GERMAN -> "12/24 Wörter"
        AppLanguage.FRENCH -> "12/24 mots"
        AppLanguage.SPANISH -> "12/24 palabras"
        AppLanguage.ITALIAN -> "12/24 parole"
        AppLanguage.ENGLISH -> "12/24 words"
    }
    val keysTitle = when (language) {
        AppLanguage.GREEK -> "Κλειδιά"
        AppLanguage.GERMAN -> "Schlüssel"
        AppLanguage.FRENCH -> "Clés"
        AppLanguage.SPANISH -> "Claves"
        AppLanguage.ITALIAN -> "Chiavi"
        AppLanguage.ENGLISH -> "Keys"
    }
    val keysSub = when (language) {
        AppLanguage.GREEK -> "ιδιωτικά"
        AppLanguage.GERMAN -> "privat"
        AppLanguage.FRENCH -> "privées"
        AppLanguage.SPANISH -> "privadas"
        AppLanguage.ITALIAN -> "private"
        AppLanguage.ENGLISH -> "private"
    }
    val addrTitle = when (language) {
        AppLanguage.GREEK -> "Διευθύνσεις"
        AppLanguage.GERMAN -> "Adressen"
        AppLanguage.FRENCH -> "Adresses"
        AppLanguage.SPANISH -> "Direcciones"
        AppLanguage.ITALIAN -> "Indirizzi"
        AppLanguage.ENGLISH -> "Addresses"
    }
    val addrSub = when (language) {
        AppLanguage.GREEK -> "δημόσιες"
        AppLanguage.GERMAN -> "öffentlich"
        AppLanguage.FRENCH -> "publiques"
        AppLanguage.SPANISH -> "públicas"
        AppLanguage.ITALIAN -> "pubblici"
        AppLanguage.ENGLISH -> "public"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GeoBox(
            title = "Seed Phrase",
            subtitle = seedSub,
            highlight = true,
            modifier = Modifier.weight(1f),
            small = true
        )

        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CopperAccent, modifier = Modifier.size(12.dp))

        GeoBox(title = "Master Key", subtitle = "BIP-32", modifier = Modifier.weight(0.9f), small = true)

        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CopperAccent, modifier = Modifier.size(12.dp))

        GeoBox(title = keysTitle, subtitle = keysSub, modifier = Modifier.weight(0.9f), small = true)

        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CopperAccent, modifier = Modifier.size(12.dp))

        GeoBox(title = addrTitle, subtitle = addrSub, modifier = Modifier.weight(1f), small = true)
    }
}

// 15. COIN VS TOKEN
@Composable
private fun CoinVsTokenDiagram(language: AppLanguage) {
    val coinTitle = when (language) {
        AppLanguage.GREEK -> "Εγγενές Coin"
        AppLanguage.GERMAN -> "Nativer Coin"
        AppLanguage.FRENCH -> "Coin Natif"
        AppLanguage.SPANISH -> "Moneda Nativa"
        AppLanguage.ITALIAN -> "Coin Nativa"
        AppLanguage.ENGLISH -> "Native Coin"
    }
    val coinSub = when (language) {
        AppLanguage.GREEK -> "BTC, ETH\nΝόμισμα πρωτοκόλλου"
        AppLanguage.GERMAN -> "BTC, ETH\nBasiseinheit Protokoll"
        AppLanguage.FRENCH -> "BTC, ETH\nActif de base"
        AppLanguage.SPANISH -> "BTC, ETH\nMoneda de protocolo"
        AppLanguage.ITALIAN -> "BTC, ETH\nAsset di base"
        AppLanguage.ENGLISH -> "BTC, ETH\nBase protocol asset"
    }
    val tokenSub = when (language) {
        AppLanguage.GREEK -> "ERC-20, USDT\nΣυμβόλαιο πάνω στο chain"
        AppLanguage.GERMAN -> "ERC-20, USDT\nVertrag auf Chain"
        AppLanguage.FRENCH -> "ERC-20, USDT\nContrat sur la chaîne"
        AppLanguage.SPANISH -> "ERC-20, USDT\nContrato en la red"
        AppLanguage.ITALIAN -> "ERC-20, USDT\nContratto on-chain"
        AppLanguage.ENGLISH -> "ERC-20, USDT\nContract on chain"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GeoBox(
            title = coinTitle,
            subtitle = coinSub,
            highlight = true,
            modifier = Modifier.weight(1f)
        )
        GeoBox(
            title = "Token",
            subtitle = tokenSub,
            modifier = Modifier.weight(1f)
        )
    }
}

// 16. SMART CONTRACT STATE
@Composable
private fun SmartContractStateDiagram(language: AppLanguage) {
    val codeTitle = when (language) {
        AppLanguage.GREEK -> "Κώδικας στο Chain"
        AppLanguage.GERMAN -> "Code auf Chain"
        AppLanguage.FRENCH -> "Code sur la chaîne"
        AppLanguage.SPANISH -> "Código en cadena"
        AppLanguage.ITALIAN -> "Codice on-chain"
        AppLanguage.ENGLISH -> "Code on Chain"
    }
    val codeSub = when (language) {
        AppLanguage.GREEK -> "έξυπνο συμβόλαιο"
        AppLanguage.GERMAN -> "Smart Contract"
        AppLanguage.FRENCH -> "Smart Contract"
        AppLanguage.SPANISH -> "Smart Contract"
        AppLanguage.ITALIAN -> "Smart Contract"
        AppLanguage.ENGLISH -> "smart contract"
    }
    val callTitle = when (language) {
        AppLanguage.GREEK -> "Κλήση Tx + Gas"
        AppLanguage.GERMAN -> "Aufruf Tx + Gas"
        AppLanguage.FRENCH -> "Appel Tx + Gas"
        AppLanguage.SPANISH -> "Llamada Tx + Gas"
        AppLanguage.ITALIAN -> "Chiamata Tx + Gas"
        AppLanguage.ENGLISH -> "Tx Call + Gas"
    }
    val callSub = when (language) {
        AppLanguage.GREEK -> "εκτέλεση EVM"
        AppLanguage.GERMAN -> "EVM-Ausführung"
        AppLanguage.FRENCH -> "exécution EVM"
        AppLanguage.SPANISH -> "ejecución EVM"
        AppLanguage.ITALIAN -> "esecuzione EVM"
        AppLanguage.ENGLISH -> "EVM execution"
    }
    val stateTitle = when (language) {
        AppLanguage.GREEK -> "Νέα Κατάσταση"
        AppLanguage.GERMAN -> "Neuer Status"
        AppLanguage.FRENCH -> "Nouvel État"
        AppLanguage.SPANISH -> "Nuevo Estado"
        AppLanguage.ITALIAN -> "Nuovo Stato"
        AppLanguage.ENGLISH -> "New State"
    }
    val stateSub = when (language) {
        AppLanguage.GREEK -> "ενημέρωση storage"
        AppLanguage.GERMAN -> "Speicher-Update"
        AppLanguage.FRENCH -> "màj du stockage"
        AppLanguage.SPANISH -> "actualizar storage"
        AppLanguage.ITALIAN -> "aggiorna storage"
        AppLanguage.ENGLISH -> "storage update"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GeoBox(
            title = codeTitle,
            subtitle = codeSub,
            modifier = Modifier.weight(1.1f),
            small = true
        )

        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CopperAccent, modifier = Modifier.size(14.dp))

        GeoBox(
            title = callTitle,
            subtitle = callSub,
            highlight = true,
            modifier = Modifier.weight(1.1f),
            small = true
        )

        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CopperAccent, modifier = Modifier.size(14.dp))

        GeoBox(
            title = stateTitle,
            subtitle = stateSub,
            modifier = Modifier.weight(1f),
            small = true
        )
    }
}

// 17. SYSTEM LIMITS
@Composable
private fun SystemLimitsDiagram(language: AppLanguage) {
    val secTitle = when (language) {
        AppLanguage.GREEK -> "2. Κόστος Ασφάλειας"
        AppLanguage.GERMAN -> "2. Sicherheitskosten"
        AppLanguage.FRENCH -> "2. Coût de Sécurité"
        AppLanguage.SPANISH -> "2. Coste de Seguridad"
        AppLanguage.ITALIAN -> "2. Costo di Sicurezza"
        AppLanguage.ENGLISH -> "2. Security Cost"
    }
    val secSub = when (language) {
        AppLanguage.GREEK -> "Ενέργεια (PoW) ή Stake"
        AppLanguage.GERMAN -> "Energie (PoW) / Stake"
        AppLanguage.FRENCH -> "Énergie (PoW) / Stake"
        AppLanguage.SPANISH -> "Energía (PoW) o Stake"
        AppLanguage.ITALIAN -> "Energia (PoW) o Stake"
        AppLanguage.ENGLISH -> "Energy or Stake"
    }
    val pseudoTitle = when (language) {
        AppLanguage.GREEK -> "3. Ψευδωνυμία"
        AppLanguage.GERMAN -> "3. Pseudonymität"
        AppLanguage.FRENCH -> "3. Pseudonymat"
        AppLanguage.SPANISH -> "3. Seudonimia"
        AppLanguage.ITALIAN -> "3. Pseudonimia"
        AppLanguage.ENGLISH -> "3. Pseudonymity"
    }
    val pseudoSub = when (language) {
        AppLanguage.GREEK -> "Δημόσιες ροές tx"
        AppLanguage.GERMAN -> "Öffentliche Tx-Flüsse"
        AppLanguage.FRENCH -> "Flux de tx publics"
        AppLanguage.SPANISH -> "Flujo público de tx"
        AppLanguage.ITALIAN -> "Flussi tx pubblici"
        AppLanguage.ENGLISH -> "Public tx flow"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GeoBox(
                title = "1. Throughput",
                subtitle = "L1 limits (need L2)",
                modifier = Modifier.weight(1f),
                small = true
            )
            GeoBox(
                title = secTitle,
                subtitle = secSub,
                modifier = Modifier.weight(1f),
                small = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GeoBox(
                title = pseudoTitle,
                subtitle = pseudoSub,
                modifier = Modifier.weight(1f),
                small = true
            )
            GeoBox(
                title = "4. Forks",
                subtitle = "Consensus divergence",
                modifier = Modifier.weight(1f),
                small = true
            )
        }
    }
}

@Composable
private fun LiquidationMechanicsDiagram(language: AppLanguage) {
    val margin = when (language) {
        AppLanguage.GREEK -> "Περιθώριο"
        AppLanguage.GERMAN -> "Margin"
        AppLanguage.FRENCH -> "Marge"
        AppLanguage.SPANISH -> "Margen"
        AppLanguage.ITALIAN -> "Margine"
        AppLanguage.ENGLISH -> "Margin"
    }
    val marginSub = when (language) {
        AppLanguage.GREEK -> "isolated ή cross"
        AppLanguage.GERMAN -> "Isolated oder Cross"
        AppLanguage.FRENCH -> "isolée ou croisée"
        AppLanguage.SPANISH -> "aislado o cruzado"
        AppLanguage.ITALIAN -> "isolato o cross"
        AppLanguage.ENGLISH -> "isolated or cross"
    }
    val floor = when (language) {
        AppLanguage.GREEK -> "Κατώφλι"
        AppLanguage.GERMAN -> "Schwelle"
        AppLanguage.FRENCH -> "Seuil"
        AppLanguage.SPANISH -> "Umbral"
        AppLanguage.ITALIAN -> "Soglia"
        AppLanguage.ENGLISH -> "Floor"
    }
    val floorSub = when (language) {
        AppLanguage.GREEK -> "maintenance"
        AppLanguage.GERMAN -> "Maintenance"
        AppLanguage.FRENCH -> "maintien"
        AppLanguage.SPANISH -> "mantenimiento"
        AppLanguage.ITALIAN -> "mantenimento"
        AppLanguage.ENGLISH -> "maintenance"
    }
    val close = when (language) {
        AppLanguage.GREEK -> "Κλείσιμο"
        AppLanguage.GERMAN -> "Schluss"
        AppLanguage.FRENCH -> "Clôture"
        AppLanguage.SPANISH -> "Cierre"
        AppLanguage.ITALIAN -> "Chiusura"
        AppLanguage.ENGLISH -> "Close"
    }
    val closeSub = when (language) {
        AppLanguage.GREEK -> "από το venue"
        AppLanguage.GERMAN -> "durch die Börse"
        AppLanguage.FRENCH -> "par la place"
        AppLanguage.SPANISH -> "por el venue"
        AppLanguage.ITALIAN -> "dal venue"
        AppLanguage.ENGLISH -> "by the venue"
    }
    ThreeStepRow(margin, marginSub, floor, floorSub, close, closeSub)
}

@Composable
private fun FundingMechanicsDiagram(language: AppLanguage) {
    val mark = when (language) {
        AppLanguage.GREEK -> "Mark"
        AppLanguage.GERMAN -> "Mark"
        AppLanguage.FRENCH -> "Mark"
        AppLanguage.SPANISH -> "Mark"
        AppLanguage.ITALIAN -> "Mark"
        AppLanguage.ENGLISH -> "Mark"
    }
    val markSub = when (language) {
        AppLanguage.GREEK -> "τιμή συμβολαίου"
        AppLanguage.GERMAN -> "Kontraktpreis"
        AppLanguage.FRENCH -> "prix du contrat"
        AppLanguage.SPANISH -> "precio del contrato"
        AppLanguage.ITALIAN -> "prezzo contratto"
        AppLanguage.ENGLISH -> "contract price"
    }
    val pay = when (language) {
        AppLanguage.GREEK -> "Funding"
        AppLanguage.GERMAN -> "Funding"
        AppLanguage.FRENCH -> "Funding"
        AppLanguage.SPANISH -> "Funding"
        AppLanguage.ITALIAN -> "Funding"
        AppLanguage.ENGLISH -> "Funding"
    }
    val paySub = when (language) {
        AppLanguage.GREEK -> "περιοδική πληρωμή"
        AppLanguage.GERMAN -> "periodische Zahlung"
        AppLanguage.FRENCH -> "paiement périodique"
        AppLanguage.SPANISH -> "pago periódico"
        AppLanguage.ITALIAN -> "pagamento periodico"
        AppLanguage.ENGLISH -> "periodic payment"
    }
    val peers = when (language) {
        AppLanguage.GREEK -> "Long ⇄ Short"
        AppLanguage.GERMAN -> "Long ⇄ Short"
        AppLanguage.FRENCH -> "Long ⇄ Short"
        AppLanguage.SPANISH -> "Long ⇄ Short"
        AppLanguage.ITALIAN -> "Long ⇄ Short"
        AppLanguage.ENGLISH -> "Long ⇄ Short"
    }
    val peersSub = when (language) {
        AppLanguage.GREEK -> "συνήθως P2P"
        AppLanguage.GERMAN -> "meist P2P"
        AppLanguage.FRENCH -> "souvent P2P"
        AppLanguage.SPANISH -> "casi siempre P2P"
        AppLanguage.ITALIAN -> "di solito P2P"
        AppLanguage.ENGLISH -> "usually P2P"
    }
    ThreeStepRow(mark, markSub, pay, paySub, peers, peersSub)
}

@Composable
private fun OrderBookDiagram(language: AppLanguage) {
    val bids = when (language) {
        AppLanguage.GREEK -> "Bids"
        AppLanguage.GERMAN -> "Bids"
        AppLanguage.FRENCH -> "Bids"
        AppLanguage.SPANISH -> "Bids"
        AppLanguage.ITALIAN -> "Bid"
        AppLanguage.ENGLISH -> "Bids"
    }
    val bidsSub = when (language) {
        AppLanguage.GREEK -> "αγορές που κάθονται"
        AppLanguage.GERMAN -> "ruhende Käufe"
        AppLanguage.FRENCH -> "achats au repos"
        AppLanguage.SPANISH -> "compras en espera"
        AppLanguage.ITALIAN -> "acquisti in attesa"
        AppLanguage.ENGLISH -> "resting buys"
    }
    val spread = when (language) {
        AppLanguage.GREEK -> "Spread"
        AppLanguage.GERMAN -> "Spread"
        AppLanguage.FRENCH -> "Spread"
        AppLanguage.SPANISH -> "Spread"
        AppLanguage.ITALIAN -> "Spread"
        AppLanguage.ENGLISH -> "Spread"
    }
    val spreadSub = when (language) {
        AppLanguage.GREEK -> "κενό τιμής"
        AppLanguage.GERMAN -> "Preislücke"
        AppLanguage.FRENCH -> "écart de prix"
        AppLanguage.SPANISH -> "hueco de precio"
        AppLanguage.ITALIAN -> "vuoto di prezzo"
        AppLanguage.ENGLISH -> "price gap"
    }
    val asks = when (language) {
        AppLanguage.GREEK -> "Asks"
        AppLanguage.GERMAN -> "Asks"
        AppLanguage.FRENCH -> "Asks"
        AppLanguage.SPANISH -> "Asks"
        AppLanguage.ITALIAN -> "Ask"
        AppLanguage.ENGLISH -> "Asks"
    }
    val asksSub = when (language) {
        AppLanguage.GREEK -> "πωλήσεις που κάθονται"
        AppLanguage.GERMAN -> "ruhende Verkäufe"
        AppLanguage.FRENCH -> "ventes au repos"
        AppLanguage.SPANISH -> "ventas en espera"
        AppLanguage.ITALIAN -> "vendite in attesa"
        AppLanguage.ENGLISH -> "resting sells"
    }
    ThreeStepRow(bids, bidsSub, spread, spreadSub, asks, asksSub, highlightMiddle = true)
}

@Composable
private fun HalvingIssuanceDiagram(language: AppLanguage) {
    val caption = when (language) {
        AppLanguage.GREEK -> "Επιδότηση ανά block"
        AppLanguage.GERMAN -> "Subvention pro Block"
        AppLanguage.FRENCH -> "Subvention par bloc"
        AppLanguage.SPANISH -> "Subvención por bloque"
        AppLanguage.ITALIAN -> "Sussidio per blocco"
        AppLanguage.ENGLISH -> "Subsidy per block"
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = caption,
            fontSize = 11.sp,
            color = DiagramTextMuted,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("50", "25", "12.5", "6.25", "3.125").forEachIndexed { index, value ->
                GeoBox(
                    title = value,
                    subtitle = "BTC",
                    modifier = Modifier.weight(1f),
                    highlight = index == 4,
                    small = true
                )
                if (index < 4) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = CopperAccent,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VenueLossCoverDiagram(language: AppLanguage) {
    val forced = when (language) {
        AppLanguage.GREEK -> "Αναγκαστικό"
        AppLanguage.GERMAN -> "Zwangsschluss"
        AppLanguage.FRENCH -> "Clôture forcée"
        AppLanguage.SPANISH -> "Cierre forzado"
        AppLanguage.ITALIAN -> "Chiusura forzata"
        AppLanguage.ENGLISH -> "Forced close"
    }
    val forcedSub = when (language) {
        AppLanguage.GREEK -> "κάτω από το κατώφλι"
        AppLanguage.GERMAN -> "unter der Schwelle"
        AppLanguage.FRENCH -> "sous le seuil"
        AppLanguage.SPANISH -> "bajo el umbral"
        AppLanguage.ITALIAN -> "sotto la soglia"
        AppLanguage.ENGLISH -> "below the floor"
    }
    val fund = when (language) {
        AppLanguage.GREEK -> "Ταμείο"
        AppLanguage.GERMAN -> "Fonds"
        AppLanguage.FRENCH -> "Fonds"
        AppLanguage.SPANISH -> "Fondo"
        AppLanguage.ITALIAN -> "Fondo"
        AppLanguage.ENGLISH -> "Fund"
    }
    val fundSub = when (language) {
        AppLanguage.GREEK -> "ασφάλιση venue"
        AppLanguage.GERMAN -> "Börsenversicherung"
        AppLanguage.FRENCH -> "assurance place"
        AppLanguage.SPANISH -> "seguro del venue"
        AppLanguage.ITALIAN -> "assicurazione venue"
        AppLanguage.ENGLISH -> "venue insurance"
    }
    val adl = when (language) {
        AppLanguage.GREEK -> "ADL"
        AppLanguage.GERMAN -> "ADL"
        AppLanguage.FRENCH -> "ADL"
        AppLanguage.SPANISH -> "ADL"
        AppLanguage.ITALIAN -> "ADL"
        AppLanguage.ENGLISH -> "ADL"
    }
    val adlSub = when (language) {
        AppLanguage.GREEK -> "αν το ταμείο δεν φτάνει"
        AppLanguage.GERMAN -> "falls Fonds fehlt"
        AppLanguage.FRENCH -> "si le fonds manque"
        AppLanguage.SPANISH -> "si el fondo no llega"
        AppLanguage.ITALIAN -> "se il fondo non basta"
        AppLanguage.ENGLISH -> "if the fund is short"
    }
    ThreeStepRow(forced, forcedSub, fund, fundSub, adl, adlSub)
}

@Composable
private fun ThreeStepRow(
    first: String,
    firstSub: String,
    second: String,
    secondSub: String,
    third: String,
    thirdSub: String,
    highlightMiddle: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        GeoBox(
            title = first,
            subtitle = firstSub,
            modifier = Modifier.weight(1f),
            small = true
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = CopperAccent,
            modifier = Modifier.size(14.dp)
        )
        GeoBox(
            title = second,
            subtitle = secondSub,
            modifier = Modifier.weight(1f),
            highlight = highlightMiddle,
            small = true
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = CopperAccent,
            modifier = Modifier.size(14.dp)
        )
        GeoBox(
            title = third,
            subtitle = thirdSub,
            modifier = Modifier.weight(1f),
            highlight = !highlightMiddle,
            small = true
        )
    }
}
