package com.example.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CoinCategory
import com.example.data.model.CryptoCoin
import com.example.data.model.Currency
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.JetBrainsMonoFont
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ProGoldBg
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SpaceGroteskFont
import com.example.ui.theme.SyneFont
import com.example.ui.theme.TachyonMint
import com.example.util.LocalAppStrings
import com.example.util.getLocalizedName
import java.util.Locale

@Composable
fun QuantumReticleBadge(
    tag: String,
    label: String,
    color: Color = QuantumCyan,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF0D0A1D))
            .border(0.8.dp, color.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "[$tag]",
            fontSize = 8.5.sp,
            fontFamily = JetBrainsMonoFont,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.8f)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = JetBrainsMonoFont,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.4.sp,
            color = color
        )
    }
}

@Composable
fun CoinAvatar(
    symbol: String,
    modifier: Modifier = Modifier
) {
    CryptoIconGraphic(
        symbol = symbol,
        modifier = modifier
    )
}

@Composable
fun LiveBeaconPill(
    modifier: Modifier = Modifier
) {
    DataFreshnessBadge(
        status = DataFreshnessStatus.LIVE,
        timeAgo = "1.2s",
        source = "Binance",
        modifier = modifier
    )
}

enum class DataFreshnessStatus {
    LIVE,
    DELAYED,
    STALE,
    OFFLINE
}

fun formatPriceAge(updatedAtMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    if (updatedAtMs <= 0L) return "—"
    val seconds = ((nowMs - updatedAtMs) / 1000L).coerceAtLeast(0L)
    return when {
        seconds < 60L -> "${seconds}s"
        seconds < 3600L -> "${seconds / 60L}m"
        else -> "${seconds / 3600L}h"
    }
}

fun freshnessFor(updatedAtMs: Long, nowMs: Long = System.currentTimeMillis()): DataFreshnessStatus {
    if (updatedAtMs <= 0L) return DataFreshnessStatus.OFFLINE
    val age = nowMs - updatedAtMs
    return when {
        age <= 45_000L -> DataFreshnessStatus.LIVE
        age <= 15 * 60_000L -> DataFreshnessStatus.DELAYED
        else -> DataFreshnessStatus.STALE
    }
}

@Composable
fun DataFreshnessBadge(
    status: DataFreshnessStatus = DataFreshnessStatus.LIVE,
    timeAgo: String = "1.2s",
    source: String = "Binance",
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current
    val (dotColor, statusText) = when (status) {
        DataFreshnessStatus.LIVE -> palette.gainColor to "LIVE"
        DataFreshnessStatus.DELAYED -> NeonAmber to "DELAYED"
        DataFreshnessStatus.STALE -> Color(0xFFF97316) to "STALE"
        DataFreshnessStatus.OFFLINE -> Color(0xFFF43F5E) to "OFFLINE"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CosmicVoidBg.copy(alpha = 0.85f))
            .border(0.8.dp, dotColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = statusText,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.4.sp,
            color = dotColor
        )
        Text(
            text = "• $timeAgo ago ($source)",
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = palette.textSecondary
        )
    }
}

@Composable
fun ExplainMetricBadge(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NeonCyan.copy(alpha = 0.12f))
            .border(0.8.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .clickable {
                com.example.util.AppSoundManager.playTechClick()
                onClick()
            }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
fun SearchBarField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    onFocus: (() -> Unit)? = null
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(palette.surface)
            .border(1.dp, palette.border, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search icon",
                tint = palette.textSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChanged,
                singleLine = true,
                textStyle = TextStyle(
                    color = palette.textPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(palette.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_coin_input")
                    .then(
                        if (onFocus != null) {
                            Modifier.onFocusChanged { if (it.isFocused) onFocus() }
                        } else {
                            Modifier
                        }
                    ),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = strings.searchPlaceholder,
                            color = palette.textSecondary,
                            fontSize = 13.5.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun TrustBar(
    modifier: Modifier = Modifier,
    latencyMs: Int = 0,
    isRefreshing: Boolean = false,
    isConnected: Boolean = true,
    sourceName: String = "Binance & CoinGecko",
    lastUpdatedMs: Long = 0L,
    onRefresh: (() -> Unit)? = null
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current

    val now = System.currentTimeMillis()
    val ageMs = if (lastUpdatedMs > 0) (now - lastUpdatedMs).coerceAtLeast(0) else Long.MAX_VALUE

    val stateText: String
    val stateColor: Color
    when {
        isRefreshing -> {
            stateText = "REFRESHING"
            stateColor = palette.primary
        }
        !isConnected -> {
            stateText = "OFFLINE"
            stateColor = Color(0xFFF43F5E)
        }
        ageMs > 15 * 60_000L -> {
            stateText = "STALE"
            stateColor = Color(0xFFF97316)
        }
        ageMs > 45_000L -> {
            stateText = "DELAYED"
            stateColor = NeonAmber
        }
        else -> {
            stateText = "LIVE"
            stateColor = palette.gainColor
        }
    }

    val latencySuffix = if (!isRefreshing && isConnected && latencyMs > 0) " · ${latencyMs}ms" else ""
    val headline = if (isRefreshing) "REFRESHING • $sourceName" else "$stateText · $sourceName$latencySuffix"
    val updatedText = when {
        isRefreshing -> "Refreshing..."
        lastUpdatedMs <= 0L -> "Not updated yet"
        else -> "Updated ${formatPriceAge(lastUpdatedMs, now)} ago"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(palette.surface)
            .border(1.dp, if (isRefreshing) palette.primary.copy(alpha = 0.5f) else palette.border, RoundedCornerShape(8.dp))
            .then(
                if (onRefresh != null) {
                    Modifier.clickable {
                        com.example.util.AppSoundManager.playTechClick()
                        onRefresh()
                    }
                } else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(stateColor)
            )
            Text(
                text = headline,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isRefreshing) palette.primary else stateColor
            )
        }
        Text(
            text = updatedText,
            fontSize = 10.sp,
            fontFamily = JetBrainsMonoFont,
            fontWeight = FontWeight.Medium,
            color = palette.textSecondary
        )
    }
}

@Composable
fun CategoryFilterPills(
    selectedCategory: CoinCategory,
    onSelectCategory: (CoinCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current

    // Ordered with Favorites first
    val orderedCategories = listOf(
        CoinCategory.FAVORITES,
        CoinCategory.ALL,
        CoinCategory.LAYER1,
        CoinCategory.LAYER2,
        CoinCategory.DEFI,
        CoinCategory.AI_INFRA,
        CoinCategory.MEME,
        CoinCategory.RWA_DEPIN,
        CoinCategory.UTILITY
    )

    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(orderedCategories.size) { index ->
            val category = orderedCategories[index]
            val isSelected = selectedCategory == category
            val label = if (category == CoinCategory.FAVORITES) "★ ${category.getLocalizedName(strings)}" else category.getLocalizedName(strings)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) palette.primary.copy(alpha = 0.15f) else palette.surface
                    )
                    .border(
                        1.dp,
                        if (isSelected) palette.primary else palette.border,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelectCategory(category) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("category_${category.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) palette.primary else palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CryptoCoinRow(
    coin: CryptoCoin,
    currency: Currency,
    isProUnlocked: Boolean,
    isCacheStale: Boolean = false,
    onCoinClicked: (CryptoCoin) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current
    val isGain = coin.change24h >= 0
    val trendColor = if (isGain) palette.gainColor else palette.lossColor
    var prevPrice by remember { mutableDoubleStateOf(coin.priceUsd) }
    val flashAlpha = remember { Animatable(0f) }
    var flashColor by remember { mutableStateOf(Color.Transparent) }

    LaunchedEffect(coin.priceUsd) {
        if (prevPrice != 0.0 && coin.priceUsd != prevPrice) {
            val isUp = coin.priceUsd > prevPrice
            flashColor = if (isUp) Color(0xFF00E676) else Color(0xFFFF1744)
            flashAlpha.snapTo(0.95f)
            flashAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
            )
        }
        prevPrice = coin.priceUsd
    }

    // Dynamic holographic shimmer duration scaled to price momentum / volatility
    val volatilityMultiplier = (kotlin.math.abs(coin.change24h).toFloat() / 4f).coerceIn(0.6f, 3.5f)
    val shimmerDuration = (2200 / volatilityMultiplier).toInt()

    val infiniteTransition = rememberInfiniteTransition(label = "coin_row_shimmer")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = shimmerDuration, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_phase"
    )
    val cometPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "comet_pulse"
    )

    val borderBrush = if (flashAlpha.value > 0.02f) {
        Brush.horizontalGradient(
            listOf(
                flashColor.copy(alpha = flashAlpha.value),
                flashColor.copy(alpha = flashAlpha.value * 0.5f),
                flashColor.copy(alpha = flashAlpha.value)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                palette.border.copy(alpha = 0.5f),
                if (isGain) Color(0xFF00F5FF).copy(alpha = 0.35f) else Color(0xFFFF1744).copy(alpha = 0.28f),
                palette.border.copy(alpha = 0.5f)
            ),
            startX = shimmerPhase * 350f,
            endX = shimmerPhase * 350f + 250f
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (flashAlpha.value > 0.05f) {
                    flashColor.copy(alpha = flashAlpha.value * 0.16f)
                } else {
                    Color(0xFF0A1324).copy(alpha = 0.78f)
                }
            )
            .border(
                width = if (flashAlpha.value > 0.05f) 1.6.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onCoinClicked(coin) }
            .padding(horizontal = 12.dp, vertical = 11.dp)
            .testTag("coin_row_${coin.symbol.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: Favorite Star + Avatar + Name + Drawdown
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = { onFavoriteToggle(coin.id) },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("fav_btn_${coin.symbol.lowercase()}")
            ) {
                Icon(
                    imageVector = if (coin.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Toggle favorite",
                    tint = if (coin.isFavorite) palette.primary else palette.textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            CoinAvatar(symbol = coin.symbol, modifier = Modifier.size(34.dp))

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f, fill = false)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = coin.name,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = coin.symbol,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textSecondary,
                        maxLines = 1
                    )
                    if (coin.symbol.equals("BTC", ignoreCase = true)) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(com.example.ui.theme.QuantumCyan.copy(alpha = 0.15f))
                                .border(0.6.dp, com.example.ui.theme.QuantumCyan.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "BTC SPOT",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.4.sp,
                                fontFamily = JetBrainsMonoFont,
                                color = com.example.ui.theme.QuantumCyan
                            )
                        }
                    } else if (coin.symbol.equals("ETH", ignoreCase = true)) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(com.example.ui.theme.SapphireBlueBright.copy(alpha = 0.15f))
                                .border(0.6.dp, com.example.ui.theme.SapphireBlueBright.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "ETH SPOT",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.4.sp,
                                fontFamily = JetBrainsMonoFont,
                                color = com.example.ui.theme.SapphireBlueBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                val capText = if (coin.isLivePrice && coin.marketCap > 0.0) {
                    coin.formattedMarketCap(currency)
                } else {
                    "—"
                }
                val volText = if (coin.isLivePrice && coin.volume24h > 0.0) {
                    coin.formattedVolume(currency)
                } else {
                    "—"
                }
                Text(
                    text = "$capText · $volText",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Center / Right: live sparkline only. Empty tape stays a dash — never a fake uptrend.
        val sparkPoints = remember(coin.sparkline) { coin.sparkline }
        if (sparkPoints.size < 2) {
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "—",
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
            }
        } else {
        Canvas(
            modifier = Modifier
                .width(52.dp)
                .height(24.dp)
                .padding(horizontal = 4.dp)
        ) {
            val w = size.width
            val h = size.height
            val minVal = sparkPoints.minOrNull() ?: 1.0
            val maxVal = sparkPoints.maxOrNull() ?: 2.0
            val range = (maxVal - minVal).coerceAtLeast(0.0001)

            val stepX = w / (sparkPoints.size - 1).coerceAtLeast(1)
            val path = Path()
            val fillPath = Path()

            var lastX = 0f
            var lastY = h / 2f

            sparkPoints.forEachIndexed { i, pt ->
                val x = i * stepX
                val normY = ((pt - minVal) / range).toFloat()
                val y = (h - 6.dp.toPx()) - (normY * (h - 10.dp.toPx())) + 2.dp.toPx()
                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, h)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
                lastX = x
                lastY = y
            }
            fillPath.lineTo(lastX, h)
            fillPath.close()

            // 1. Soft Area Gradient Underglow
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        trendColor.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // 2. Wide Neon Diffuse Glow Stroke
            drawPath(
                path = path,
                color = trendColor.copy(alpha = 0.45f),
                style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 3. Crisp Neon Core Laser Stroke
            drawPath(
                path = path,
                color = if (isGain) Color(0xFFE0FFFF) else Color(0xFFFFD1D9),
                style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 4. Glowing Comet Head at Leading Point
            val cometGlowRadius = 5.5.dp.toPx() * cometPulse
            drawCircle(
                color = trendColor.copy(alpha = 0.35f * (2f - cometPulse).coerceIn(0.5f, 1f)),
                radius = cometGlowRadius,
                center = Offset(lastX, lastY)
            )
            drawCircle(
                color = trendColor,
                radius = 2.8.dp.toPx(),
                center = Offset(lastX, lastY)
            )
            drawCircle(
                color = Color.White,
                radius = 1.3.dp.toPx(),
                center = Offset(lastX, lastY)
            )
        }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right side: Large Tabular Price & 24h %
        val staleAlpha = if (isCacheStale) {
            val transition = rememberInfiniteTransition(label = "stale_price_pulse")
            val alpha by transition.animateFloat(
                initialValue = 0.35f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "stale_alpha"
            )
            alpha
        } else {
            1.0f
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = coin.displayPrice(currency),
                fontSize = if (coin.quoteState == com.example.data.model.QuoteState.LIVE) 14.5.sp else 12.sp,
                fontFamily = JetBrainsMonoFont,
                fontWeight = FontWeight.Bold,
                color = if (coin.isLivePrice) palette.textPrimary else QuantumCyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.graphicsLayer(alpha = staleAlpha)
            )
            Spacer(modifier = Modifier.height(2.dp))
            val changeFormatted = if (coin.isLivePrice) {
                com.example.util.AppNumberFormatter.formatPercent(coin.change24h, includeSign = true, decimals = 1)
            } else {
                "—"
            }
            Text(
                text = changeFormatted,
                fontSize = 11.5.sp,
                fontFamily = JetBrainsMonoFont,
                fontWeight = FontWeight.Bold,
                color = if (coin.isLivePrice) trendColor else palette.textPrimary.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Clean, structured 3-line micro-copy explainer under individual metrics:
 * 1. What it is
 * 2. What it shows
 * 3. What it does not mean (Zero buy/sell bias)
 */
@Composable
fun MetricExplainerBox(
    whatItIs: String,
    whatItShows: String,
    whatItDoesNotMean: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (palette.isLight) Color(0xFFF1F5F9) else CosmicVoidSurfaceElevated)
            .border(1.dp, palette.border.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = "What it is: ",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = palette.primary
            )
            Text(
                text = whatItIs,
                fontSize = 10.5.sp,
                color = palette.textSecondary,
                lineHeight = 14.sp
            )
        }
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = "What it shows: ",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = palette.gainColor
            )
            Text(
                text = whatItShows,
                fontSize = 10.5.sp,
                color = palette.textSecondary,
                lineHeight = 14.sp
            )
        }
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = "Not a signal: ",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textMuted
            )
            Text(
                text = whatItDoesNotMean,
                fontSize = 10.5.sp,
                color = palette.textMuted,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun EmptyStateMessage(
    title: String,
    subtitle: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surface)
            .border(1.dp, palette.border, RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                color = palette.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.Button(
                    onClick = onRetry,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = palette.primary.copy(alpha = 0.2f),
                        contentColor = palette.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Retry Connection", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

