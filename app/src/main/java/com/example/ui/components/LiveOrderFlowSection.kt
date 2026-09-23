package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoCoin
import com.example.data.model.Currency
import com.example.ui.theme.PhotonGoldBright
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.QuantumCyanBright
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.SoftEmerald
import com.example.ui.theme.TextCyanSlate
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.holographicCard
import com.example.util.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LiveOrderExecution(
    val id: String,
    val timestamp: String,
    val symbol: String,
    val isBuy: Boolean,
    val amount: String,
    val valueUsd: Double,
    val exchange: String
)

@Composable
fun LiveOrderFlowSection(
    activeCoin: CryptoCoin,
    currency: Currency,
    recentTrades: List<com.example.data.model.FuturesTrade> = emptyList(),
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    val orderHistory = remember(recentTrades, activeCoin.symbol) {
        recentTrades.take(6).map { trade ->
            val rawAmt = trade.qty
            val amt = if (trade.price >= 1000.0) {
                com.example.util.AppNumberFormatter.formatRawPrice(rawAmt, decimals = 3).removePrefix("$")
            } else if (trade.price >= 10.0) {
                com.example.util.AppNumberFormatter.formatRawPrice(rawAmt, decimals = 1).removePrefix("$")
            } else {
                com.example.util.AppNumberFormatter.formatRawPrice(rawAmt, decimals = 0).removePrefix("$")
            }
            LiveOrderExecution(
                id = trade.id.toString(),
                timestamp = timeFormat.format(Date(trade.timeMs)),
                symbol = trade.symbol.removeSuffix("USDT"),
                isBuy = !trade.isSell,
                amount = amt,
                valueUsd = trade.valueUsd,
                exchange = "Binance USDT-M"
            )
        }
    }
    val buyDominance = remember(recentTrades) {
        val buys = recentTrades.filter { !it.isSell }.sumOf { it.valueUsd }
        val total = recentTrades.sumOf { it.valueUsd }
        if (total > 0.0) ((buys / total) * 100.0).toFloat() else 50f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .holographicCard(
                shape = RoundedCornerShape(22.dp),
                glowColor = QuantumCyan,
                pulseColor = PhotonGoldBright
            )
            .padding(16.dp)
            .testTag("live_order_flow_section")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header with 24/7 Quantum Live Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SoftEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.liveOrderFlowTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = QuantumCyan
                    )
                }
            }

            Text(
                text = strings.liveOrderFlowSub,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = TextCyanSlate
            )

            // Order Flow Dual Plasma Waveform
            val totalVolumeUsd = remember(orderHistory.size) { orderHistory.sumOf { it.valueUsd } }
            val avgTradeSize = remember(orderHistory.size) { if (orderHistory.isNotEmpty()) totalVolumeUsd / orderHistory.size else 50000.0 }
            val volumeSpikeFactor = (avgTradeSize / 60000.0).toFloat().coerceIn(0.8f, 2.5f)
            val tradeVelocity = 2.5f

            QuantumOrderFlowWave(
                buyRatio = buyDominance,
                sellRatio = 100f - buyDominance,
                volumeSpikeFactor = volumeSpikeFactor,
                tradeVelocity = tradeVelocity,
                height = 76.dp
            )

            // Real-Time Buy vs Sell Dominance Pressure Gauge
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Buy",
                            tint = SoftEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val buyDomFormatted = com.example.util.AppNumberFormatter.formatPercent(buyDominance.toDouble(), includeSign = false, decimals = 1)
                        Text(
                            text = "${strings.buyDominanceLabel}: $buyDomFormatted",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmerald
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val sellDomFormatted = com.example.util.AppNumberFormatter.formatPercent((100f - buyDominance).toDouble(), includeSign = false, decimals = 1)
                        Text(
                            text = "${strings.sellDominanceLabel}: $sellDomFormatted",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftCrimson
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = "Sell",
                            tint = SoftCrimson,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Dual-Gradient Split Progress Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SoftCrimson.copy(alpha = 0.85f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(buyDominance / 100f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SoftEmerald, QuantumCyan)
                                )
                            )
                    )
                }
            }

            // Recent Live Executions Tape (Top 4 Micro-Orders)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = strings.liveExecutionFeed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextCyanSlate
                )

                orderHistory.take(4).forEach { order ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (order.isBuy) Color(0xFF071D18) else Color(0xFF220C12))
                            .border(
                                1.dp,
                                if (order.isBuy) SoftEmerald.copy(alpha = 0.35f) else SoftCrimson.copy(alpha = 0.35f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (order.isBuy) SoftEmerald else SoftCrimson)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (order.isBuy) "BUY" else "SELL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                CoinAvatar(
                                    symbol = order.symbol,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${order.amount} ${order.symbol}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPureWhite
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = com.example.util.AppNumberFormatter.formatPrice(order.valueUsd, currency, decimals = 0),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.isBuy) SoftEmerald else SoftCrimson
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = order.timestamp,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextCyanSlate
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
