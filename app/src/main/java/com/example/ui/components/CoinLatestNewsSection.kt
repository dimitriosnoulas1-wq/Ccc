package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoCoin
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.DrawdownRed
import com.example.ui.theme.GainGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AppNumberFormatter
import com.example.util.CoinNewsItem
import com.example.util.CoinNewsManager
import com.example.util.NewsLoadState
import com.example.util.LocalAppStrings

@Composable
fun CoinLatestNewsSection(
    coin: CryptoCoin,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var newsList by remember(coin.symbol) { mutableStateOf<List<CoinNewsItem>>(emptyList()) }
    var isLoading by remember(coin.symbol) { mutableStateOf(true) }
    var newsState by remember(coin.symbol) { mutableStateOf(NewsLoadState.EMPTY) }

    LaunchedEffect(coin.symbol) {
        isLoading = true
        val (state, fetched) = CoinNewsManager.load(coin.symbol, coin.name, coin.change24h)
        newsState = state
        newsList = fetched
        isLoading = false
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = strings.latestNewsTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = strings.latestNewsSubtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CosmicSurfaceElevated)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = coin.symbol.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }
        }

        // Horizontal Carousel (LazyRow)
        if (isLoading) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(3) {
                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CosmicSurface.copy(alpha = 0.5f))
                            .border(1.dp, CosmicBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(width = 90.dp, height = 14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CosmicSurfaceElevated)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CosmicSurfaceElevated)
                            )
                        }
                    }
                }
            }
        } else if (newsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CosmicSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (newsState == NewsLoadState.UNAVAILABLE) {
                        "News feed is temporarily unavailable."
                    } else {
                        "No recent news available for ${coin.symbol}."
                    },
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(newsList, key = { it.id }) { item ->
                    val isGain = item.deltaPct >= 0
                    val deltaFormatted = AppNumberFormatter.formatPercent(item.deltaPct, includeSign = true, decimals = 2)

                    Box(
                        modifier = Modifier
                            .width(270.dp)
                            .height(135.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CosmicSurface)
                            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                try {
                                    val parsedUri = Uri.parse(item.url)
                                    val customTabsIntent = CustomTabsIntent.Builder().build()
                                    customTabsIntent.launchUrl(context, parsedUri)
                                } catch (_: Exception) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                            }
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Row: Publisher & Time Ago
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(NeonCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Article,
                                            contentDescription = item.publisher,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.publisher,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Time",
                                        tint = TextMuted,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = item.timeAgo,
                                        fontSize = 10.5.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            // Middle: Headline (truncated to 2 lines)
                            Text(
                                text = item.title,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 17.sp,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Bottom: Ticker & Delta tag + Launch icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                       .clip(RoundedCornerShape(6.dp))
                                       .background(CosmicSurfaceElevated)
                                       .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${item.ticker} $deltaFormatted",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGain) GainGreen else DrawdownRed
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Launch,
                                    contentDescription = "Open in browser",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
