package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFont
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.MauveDeep
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SpaceGroteskFont
import com.example.ui.theme.SyneFont
import com.example.ui.theme.TachyonMint
import kotlinx.coroutines.delay

/**
 * 2126 Quantum Launch Sequence (Splash / Boot screen)
 * High-performance, 100% native Jetpack Compose implementation
 * Recreates the holographic quantum reactor, counter-spinning orbital rings,
 * live telemetry stream, and laser progress bar with 0ms lag.
 */
@Composable
fun QuantumLaunchSequenceScreen(
    onLaunchComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stages = remember {
        listOf(
            "INITIALIZING QUANTUM CORE...",
            "WARP SYNAPSE: 0.001PS // CONNECTED",
            "ENTANGLEMENT VERIFIED: 99.999%",
            "LOADING 4D FRACTAL CYCLE NODES...",
            "CRYPTOCYCLES QUANTUM // LAUNCHING"
        )
    }

    var stageIndex by remember { mutableIntStateOf(0) }
    var progress by remember { mutableFloatStateOf(0f) }

    // Progress and stage timer
    LaunchedEffect(Unit) {
        val totalDurationMs = 3200L
        val stepMs = 50L
        val totalSteps = (totalDurationMs / stepMs).toInt()

        for (step in 1..totalSteps) {
            delay(stepMs)
            progress = (step.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
            val currentStage = ((step.toFloat() / totalSteps.toFloat()) * (stages.size - 1)).toInt().coerceIn(0, stages.size - 1)
            if (currentStage != stageIndex) {
                stageIndex = currentStage
            }
        }
        delay(150)
        onLaunchComplete()
    }

    // Infinite animations for the quantum gyroscope
    val infiniteTransition = rememberInfiniteTransition(label = "quantum_animations")

    val ring1Rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1_rotation"
    )

    val ring2Rotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2_rotation"
    )

    val corePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    val pingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ping_alpha"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 80, easing = LinearEasing),
        label = "progress_bar_anim"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0E0922),
                        Color(0xFF05050F),
                        Color(0xFF020208)
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                // Tap anywhere to immediately skip into app
                onLaunchComplete()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── TOP SYSTEM TELEMETRY BAR ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp,
                        color = Color(0xFF232033).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(0.dp)
                    )
                    .background(Color(0xFF0A0814).copy(alpha = 0.8f))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .graphicsLayer { alpha = pingAlpha }
                            .background(TachyonMint, CircleShape)
                    )
                    Text(
                        text = "ENTANGLEMENT: 99.999%",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuantumCyan,
                            letterSpacing = 1.5.sp
                        )
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "CHRONO-QUANTUM",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFont,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MauveAurora
                        )
                    )
                    Text(
                        text = "//",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFont,
                            fontSize = 10.sp,
                            color = Color(0xFF475569)
                        )
                    )
                    Text(
                        text = "PORT 0x01",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFont,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            // ── CENTER STAGE: HOLOGRAPHIC QUANTUM REACTOR ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Ambient backlight glow behind gyroscope
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Backlight radial blur circle
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            QuantumCyan.copy(alpha = 0.25f),
                                            MauveDeep.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                            }
                    )

                    // Outer Gyro Ring 1 (Dashed + Rotating nodes)
                    Canvas(
                        modifier = Modifier
                            .size(220.dp)
                            .graphicsLayer { rotationZ = ring1Rotation }
                    ) {
                        val stroke = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        )
                        drawCircle(
                            color = QuantumCyan.copy(alpha = 0.35f),
                            style = stroke
                        )
                        // Top Cyan satellite node
                        drawCircle(
                            color = QuantumCyan,
                            radius = 5.dp.toPx(),
                            center = center.copy(y = center.y - size.height / 2)
                        )
                        // Bottom Mauve satellite node
                        drawCircle(
                            color = MauveAurora,
                            radius = 4.dp.toPx(),
                            center = center.copy(y = center.y + size.height / 2)
                        )
                    }

                    // Outer Gyro Ring 2 (Counter-rotating + Mint nodes)
                    Canvas(
                        modifier = Modifier
                            .size(175.dp)
                            .graphicsLayer { rotationZ = ring2Rotation }
                    ) {
                        val stroke = Stroke(width = 1.dp.toPx())
                        drawCircle(
                            color = MauveDeep.copy(alpha = 0.40f),
                            style = stroke
                        )
                        // Left Mint node
                        drawCircle(
                            color = TachyonMint,
                            radius = 4.5.dp.toPx(),
                            center = center.copy(x = center.x - size.width / 2)
                        )
                        // Right Cyan node
                        drawCircle(
                            color = QuantumCyan,
                            radius = 3.5.dp.toPx(),
                            center = center.copy(x = center.x + size.width / 2)
                        )
                    }

                    // Cardinal Markers (N, S, W, E)
                    Box(modifier = Modifier.size(220.dp)) {
                        Text(
                            text = "N",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFont,
                                fontSize = 9.sp,
                                color = QuantumCyan.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 2.dp)
                        )
                        Text(
                            text = "S",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFont,
                                fontSize = 9.sp,
                                color = QuantumCyan.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp)
                        )
                        Text(
                            text = "W",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFont,
                                fontSize = 9.sp,
                                color = QuantumCyan.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 2.dp)
                        )
                        Text(
                            text = "E",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFont,
                                fontSize = 9.sp,
                                color = QuantumCyan.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp)
                        )
                    }

                    // Central Hologram Card Core
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(corePulseScale)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF14121E).copy(alpha = 0.85f),
                                        Color(0xFF0A0914).copy(alpha = 0.95f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        QuantumCyan.copy(alpha = 0.7f),
                                        MauveAurora.copy(alpha = 0.4f),
                                        QuantumCyan.copy(alpha = 0.7f)
                                    )
                                ),
                                shape = RoundedCornerShape(22.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "CC",
                                style = TextStyle(
                                    fontFamily = SyneFont,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            QuantumCyan,
                                            Color.White,
                                            TachyonMint
                                        )
                                    ),
                                    letterSpacing = (-1).sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ENTANGLED",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFont,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = QuantumCyan.copy(alpha = 0.9f),
                                    letterSpacing = 2.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // App Title
                Text(
                    text = "CRYPTOCYCLES",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = SyneFont,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 3.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "QUANTUM SUB-SPACE COMMAND",
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuantumCyan,
                        letterSpacing = 2.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "4D Halving Fractals • Deep Space Telemetry",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFont,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── LIVE INITIALIZATION TELEMETRY SEQUENCE ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.72f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Laser Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1C1A26))
                            .border(0.5.dp, Color(0xFF2E2A3F), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            QuantumCyan,
                                            TachyonMint,
                                            MauveAurora
                                        )
                                    )
                                )
                        )
                    }

                    // Real-Time Terminal Log Feed
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .graphicsLayer { alpha = pingAlpha }
                                .background(TachyonMint, CircleShape)
                        )
                        Text(
                            text = stages.getOrElse(stageIndex) { "LAUNCHING..." },
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFont,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TachyonMint
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "CALIBRATING TENSOR MEMPOOL // EPOCH 33",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFont,
                            fontSize = 9.sp,
                            color = Color(0xFF64748B)
                        )
                    )
                }
            }

            // ── BOTTOM SECURITY & HARDWARE RIG BADGE ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0D18).copy(alpha = 0.95f))
                    .border(
                        width = 0.5.dp,
                        color = Color(0xFF232033),
                        shape = RoundedCornerShape(0.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🔒",
                        fontSize = 11.sp
                    )
                    Text(
                        text = "ZERO-KYC COLD LINK",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFont,
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "●",
                        color = TachyonMint,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "STABLE KERNEL",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFont,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFCBD5E1)
                        )
                    )
                }
            }
        }
    }
}
