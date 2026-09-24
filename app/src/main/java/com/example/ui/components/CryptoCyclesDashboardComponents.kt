package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.QuantumCyan
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.GoldPrimary
import com.example.util.LocalAppStrings
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ==========================================
// 1. TOP APP BAR
// ==========================================
@Composable
fun DashboardTopAppBar(
    isProUnlocked: Boolean = true,
    isRefreshing: Boolean = false,
    volatilityMultiplier: Float = 1.0f,
    unreadAlertsCount: Int = 0,
    onAlertHistoryClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onProBadgeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current

    val refreshTransition = rememberInfiniteTransition(label = "top_bar_refresh_spin")
    val refreshRotation by refreshTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    // Data-driven rotation velocity scaling with aggregate market volatility
    val orbDuration = (5000 / volatilityMultiplier.coerceIn(0.4f, 4.0f)).toInt()
    val orbRotation by refreshTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(orbDuration, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rotation_velocity"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Terminal Monogram Logo & App Title with Data-Driven Spinning Diamond Orb
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer rotating volatility-bound Diamond / Halo
                    Canvas(modifier = Modifier.size(36.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val r = size.width * 0.44f

                        withTransform({
                            rotate(degrees = orbRotation, pivot = Offset(cx, cy))
                        }) {
                            // Diamond energy perimeter
                            val diamondPath = Path().apply {
                                moveTo(cx, cy - r)
                                lineTo(cx + r, cy)
                                lineTo(cx, cy + r)
                                lineTo(cx - r, cy)
                                close()
                            }
                            drawPath(
                                path = diamondPath,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF00D2FF),
                                        Color(0xFF00E676),
                                        Color(0xFFFFD600),
                                        Color(0xFF00D2FF)
                                    ),
                                    center = Offset(cx, cy)
                                ),
                                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Radiant orbital node
                            drawCircle(
                                color = Color(0xFF00D2FF),
                                radius = 2.dp.toPx(),
                                center = Offset(cx, cy - r)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(palette.surface)
                            .border(1.dp, palette.border, RoundedCornerShape(7.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CC",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = palette.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    val neonGradient = remember {
                        Brush.horizontalGradient(
                            listOf(
                                NeonAmber,
                                Color(0xFF00F5FF),
                                NeonAmber
                            )
                        )
                    }
                    Text(
                        text = "CryptoCycles",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        style = androidx.compose.ui.text.TextStyle(brush = neonGradient),
                        maxLines = 1,
                        softWrap = false
                    )
                    val strings = LocalAppStrings.current
                    Text(
                        text = strings.institutionalTerminal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        style = androidx.compose.ui.text.TextStyle(brush = neonGradient),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right Actions: Refresh, Bell, Search, Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val strings = LocalAppStrings.current

                // Refresh Button
                IconButton(
                    onClick = {
                        com.example.util.AppSoundManager.playTechClick()
                        onRefreshClick()
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("top_bar_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = strings.refreshTooltip,
                        tint = if (isRefreshing) palette.primary else palette.textSecondary,
                        modifier = Modifier
                            .size(19.dp)
                            .then(if (isRefreshing) Modifier.rotate(refreshRotation) else Modifier)
                    )
                }

                // 30D Alert Audit History Bell Icon
                IconButton(
                    onClick = {
                        com.example.util.AppSoundManager.playTechClick()
                        onAlertHistoryClick()
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("top_bar_alert_history_button")
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Alert History",
                            tint = if (unreadAlertsCount > 0) palette.primary else palette.textSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                        if (unreadAlertsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(com.example.ui.theme.NeonCyan, CircleShape)
                            )
                        }
                    }
                }

                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.border, CircleShape)
                        .clickable { onProfileClick() }
                        .testTag("top_bar_profile_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // Row 2: UPGRADE TO PRO / Pro Badge (Right-aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val strings = LocalAppStrings.current
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(GoldPrimary.copy(alpha = 0.15f))
                    .border(0.8.dp, GoldPrimary, RoundedCornerShape(6.dp))
                    .clickable { onProBadgeClick() }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                    .testTag("top_bar_pro_badge")
            ) {
                Text(
                    text = if (isProUnlocked) strings.proActive else "⚡ UPGRADE TO PRO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
