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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.data.repository.VolumeProfile
import com.example.data.repository.VolumeProfileRange
import com.example.data.repository.VolumeProfileRepository
import com.example.ui.theme.JetBrainsMonoFont
import com.example.ui.theme.MarketRed
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SoftEmerald
import com.example.ui.theme.SpaceGroteskFont
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.holographicCard
import com.example.util.AppNumberFormatter
import kotlinx.coroutines.delay

private val OutsideBar = Color(0xFF475569)

@Composable
fun VolumeProfileCard(
    symbol: String,
    livePrice: Double,
    currency: Currency,
    greek: Boolean,
    modifier: Modifier = Modifier
) {
    var range by remember { mutableStateOf(VolumeProfileRange.WEEK) }
    var profile by remember(symbol, range) { mutableStateOf<VolumeProfile?>(null) }
    var loading by remember(symbol, range) { mutableStateOf(true) }

    LaunchedEffect(symbol, range) {
        while (true) {
            val fresh = VolumeProfileRepository.load(symbol, range)
            if (fresh != null) profile = fresh
            loading = false
            delay(120_000L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .holographicCard(shape = RoundedCornerShape(22.dp), glowColor = QuantumCyan, pulseColor = PhotonGold)
            .padding(16.dp)
            .testTag("volume_profile_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "VOLUME PROFILE · $symbol",
                        color = TextPrimary, fontFamily = SpaceGroteskFont,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp
                    )
                    Text(
                        if (greek) "Πού έγινε ο περισσότερος όγκος · Binance" else "Where the most volume traded · Binance",
                        color = TextMuted, fontSize = 11.sp
                    )
                }
                VolumeProfileRange.entries.forEach { r ->
                    RangeChip(if (greek) r.labelEl else r.labelEn, r == range) { range = r }
                    Spacer(Modifier.width(6.dp))
                }
            }

            val p = profile
            when {
                p == null && loading -> Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = QuantumCyan, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                }
                p == null -> Text(
                    if (greek) "Δεν υπάρχουν δεδομένα όγκου Binance για το $symbol." else "No Binance volume data for $symbol.",
                    color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 24.dp)
                )
                else -> {
                    val price = if (livePrice > 0.0) livePrice else p.lastPrice
                    ProfileChart(p, price, currency)
                    Legend(greek)
                    LevelsRow(p, currency, greek)
                    Text(
                        reading(p, price, currency, greek),
                        color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RangeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text,
        color = if (selected) Color.Black else TextSecondary,
        fontFamily = JetBrainsMonoFont, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) QuantumCyan else Color.Transparent)
            .border(1.dp, QuantumCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun ProfileChart(p: VolumeProfile, price: Double, currency: Currency) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, fontFamily = JetBrainsMonoFont, fontWeight = FontWeight.Bold)
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        val labelW = 86.dp.toPx()
        val chartW = size.width - labelW
        val top = p.bins.last().high
        val bottom = p.bins.first().low
        val span = top - bottom
        fun y(v: Double) = (size.height * (1 - (v - bottom) / span)).toFloat()
        val maxVol = p.bins.maxOf { it.volume }
        val rowH = size.height / p.bins.size
        val gap = (rowH * 0.18f).coerceAtMost(2.dp.toPx())

        p.bins.forEachIndexed { i, b ->
            val w = (b.volume / maxVol * chartW).toFloat().coerceAtLeast(1f)
            val color = when {
                i == p.pocIndex -> PhotonGold
                i in p.vaLowIndex..p.vaHighIndex -> QuantumCyan.copy(alpha = 0.75f)
                else -> OutsideBar.copy(alpha = 0.6f)
            }
            drawRoundRect(
                color, Offset(0f, y(b.high) + gap / 2), Size(w, rowH - gap),
                CornerRadius(2.dp.toPx())
            )
        }

        val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
        fun level(v: Double, color: Color, text: String, dashed: Boolean) {
            val yy = y(v).coerceIn(0f, size.height)
            drawLine(color, Offset(0f, yy), Offset(chartW, yy), 1.2.dp.toPx(), pathEffect = if (dashed) dash else null)
            val layout = measurer.measure(text, labelStyle.copy(color = color))
            val ty = (yy - layout.size.height / 2f).coerceIn(0f, size.height - layout.size.height)
            drawText(layout, topLeft = Offset(chartW + 6.dp.toPx(), ty))
        }
        level(p.valueAreaHigh, QuantumCyan.copy(alpha = 0.8f), "VAH", true)
        level(p.valueAreaLow, QuantumCyan.copy(alpha = 0.8f), "VAL", true)
        level(p.poc, PhotonGold, "POC", false)
        if (price in bottom..top) {
            level(price, TextPrimary, AppNumberFormatter.formatPrice(price, currency), false)
        }
    }
}

@Composable
private fun Legend(greek: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendDot(PhotonGold, "POC")
        LegendDot(QuantumCyan, if (greek) "Περιοχή αξίας 70%" else "Value area 70%")
        LegendDot(TextPrimary, if (greek) "Τιμή τώρα" else "Price now")
    }
}

@Composable
private fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(Modifier.width(5.dp))
        Text(text, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun LevelsRow(p: VolumeProfile, currency: Currency, greek: Boolean) {
    val buyShare = p.bins.sumOf { it.buyVolume } / p.totalVolume * 100.0
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Level("VAH", AppNumberFormatter.formatPrice(p.valueAreaHigh, currency), QuantumCyan)
        Level("POC", AppNumberFormatter.formatPrice(p.poc, currency), PhotonGold)
        Level("VAL", AppNumberFormatter.formatPrice(p.valueAreaLow, currency), QuantumCyan)
        Level(
            if (greek) "Αγοραστές" else "Buyers",
            "${AppNumberFormatter.formatRawPrice(buyShare, decimals = 0)}%",
            if (buyShare >= 50.0) SoftEmerald else MarketRed
        )
    }
}

@Composable
private fun Level(label: String, value: String, color: Color) {
    Column {
        Text(label, color = TextMuted, fontSize = 10.sp, fontFamily = JetBrainsMonoFont)
        Text(value, color = color, fontSize = 13.sp, fontFamily = JetBrainsMonoFont, fontWeight = FontWeight.Bold)
    }
}

private fun reading(p: VolumeProfile, price: Double, currency: Currency, greek: Boolean): String {
    val poc = AppNumberFormatter.formatPrice(p.poc, currency)
    val vah = AppNumberFormatter.formatPrice(p.valueAreaHigh, currency)
    val val_ = AppNumberFormatter.formatPrice(p.valueAreaLow, currency)
    return when {
        price > p.valueAreaHigh -> if (greek)
            "Η τιμή είναι πάνω από την περιοχή αξίας, πιο ψηλά από εκεί που άλλαξε χέρια ο περισσότερος όγκος. Οι traders συνήθως κοιτάνε το VAH ($vah) ως το πρώτο επίπεδο από κάτω. Όχι πρόβλεψη."
        else
            "Price is above the value area, higher than where most of the volume changed hands. Traders usually watch the VAH ($vah) as the first level below. Not a forecast."
        price < p.valueAreaLow -> if (greek)
            "Η τιμή είναι κάτω από την περιοχή αξίας, πιο χαμηλά από εκεί που άλλαξε χέρια ο περισσότερος όγκος. Οι traders συνήθως κοιτάνε το VAL ($val_) ως το πρώτο επίπεδο από πάνω. Όχι πρόβλεψη."
        else
            "Price is below the value area, lower than where most of the volume changed hands. Traders usually watch the VAL ($val_) as the first level above. Not a forecast."
        else -> if (greek)
            "Η τιμή είναι μέσα στην περιοχή αξίας ($val_ – $vah), εκεί που έγινε το 70% του όγκου. Το POC στο $poc είναι η τιμή με τον περισσότερο όγκο. Όχι πρόβλεψη."
        else
            "Price is inside the value area ($val_ – $vah), where 70% of the volume traded. The POC at $poc is the single most-traded price. Not a forecast."
    }
}
