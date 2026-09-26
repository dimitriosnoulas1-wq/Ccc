package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AggregatedDerivativesSnapshot
import com.example.data.model.DerivativesFreshness
import com.example.ui.theme.JetBrainsMonoFont
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SpaceGroteskFont
import com.example.ui.theme.SyneFont
import com.example.ui.theme.TachyonMint
import com.example.util.CycleFractalData
import com.example.util.CycleReadingText
import com.example.util.WhereWeAreReading

@Composable
fun WhereWeAreCard(
    reading: CycleFractalData?,
    priceUsd: Double,
    priceIsLive: Boolean,
    derivatives: AggregatedDerivativesSnapshot?,
    isProUnlocked: Boolean,
    greek: Boolean,
    onOpenChart: () -> Unit,
    onOpenProModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current
    val tapeLive = derivatives != null &&
        derivatives.freshness != DerivativesFreshness.UNAVAILABLE &&
        derivatives.symbol.contains("BTC", ignoreCase = true)
    val agg = derivatives?.aggregated
    val liq = if (tapeLive) {
        listOfNotNull(agg?.longLiqUsd, agg?.shortLiqUsd).sum().takeIf { it > 0.0 }
    } else {
        null
    }
    val view = WhereWeAreReading.build(
        day = reading?.currentDay,
        priceUsd = priceUsd,
        priceIsLive = priceIsLive,
        multiple2012 = reading?.multiple2012,
        multiple2016 = reading?.multiple2016,
        multiple2020 = reading?.multiple2020,
        fundingRate = if (tapeLive) agg?.fundingRate else null,
        openInterestUsd = if (tapeLive) agg?.openInterestUsd else null,
        liquidationUsd = liq
    )
    val rim = remember {
        Brush.linearGradient(listOf(NeonAmber, QuantumCyan, Color(0xFF7C5CFF), NeonAmber))
    }
    val well = remember {
        Brush.verticalGradient(listOf(Color(0xFF102E36), Color(0xFF070712)))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(18.dp, RoundedCornerShape(28.dp), ambientColor = QuantumCyan.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(28.dp))
            .background(well)
            .border(1.4.dp, rim, RoundedCornerShape(28.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = if (greek) "ΠΟΥ ΕΙΜΑΣΤΕ" else "WHERE WE ARE",
            fontFamily = SpaceGroteskFont,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.4.sp,
            color = NeonAmber
        )
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = view.dayText,
                fontFamily = SyneFont,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 72.sp
            )
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                Text(
                    text = if (greek) "ημέρες" else "days",
                    fontFamily = SpaceGroteskFont,
                    fontSize = 16.sp,
                    color = QuantumCyan
                )
                Text(
                    text = if (greek) "από το halving" else "since the halving",
                    fontSize = 12.sp,
                    color = palette.textMuted
                )
            }
        }
        Text(
            text = view.nowMultiple,
            fontFamily = SyneFont,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = TachyonMint
        )
        Text(
            text = if (greek) {
                "την τιμή της ημέρας του halving"
            } else {
                "the halving-day price"
            },
            fontSize = 13.sp,
            color = palette.textSecondary
        )

        if (isProUnlocked) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PastCyclePill("2012", view.multiple2012, Modifier.weight(1f))
                PastCyclePill("2016", view.multiple2016, Modifier.weight(1f))
                PastCyclePill("2020", view.multiple2020, Modifier.weight(1f))
            }
            Text(
                text = if (greek) "ίδια ημέρα κύκλου" else "same cycle day",
                fontSize = 11.sp,
                color = palette.textMuted
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TapeChip(if (greek) "Funding" else "Funding", view.tape.fundingPct, Modifier.weight(1f))
                TapeChip(if (greek) "OI" else "OI", view.tape.openInterest, Modifier.weight(1f))
                TapeChip(if (greek) "Liq" else "Liq", view.tape.liquidations, Modifier.weight(1f))
            }
            Text(
                text = if (greek) "Ταινία Binance · ${view.familyEl}." else "Binance tape · ${view.familyEn}.",
                fontSize = 11.sp,
                color = palette.textMuted
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .clickable(onClick = onOpenProModal)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp))
                Text(
                    text = if (greek) {
                        "2012, 2016, 2020 και η ταινία Binance είναι στο Pro."
                    } else {
                        "2012, 2016, 2020 and the Binance tape are in Pro."
                    },
                    fontSize = 13.sp,
                    color = palette.textPrimary
                )
            }
        }

        Text(
            text = CycleReadingText.disclaimer(greek),
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = palette.textMuted
        )
        Row(
            modifier = Modifier.clickable(onClick = onOpenChart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Outlined.ShowChart, contentDescription = null, tint = QuantumCyan, modifier = Modifier.size(16.dp))
            Text(
                text = if (greek) "Άνοιγμα γραφήματος" else "Open the chart",
                fontFamily = SpaceGroteskFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = QuantumCyan
            )
        }
    }
}

@Composable
private fun PastCyclePill(year: String, multiple: String, modifier: Modifier = Modifier) {
    // Same cycle colour as the rainbow and cycle charts.
    val accent = com.example.ui.rainbow.RainbowModel.HALVINGS
        .firstOrNull { it.year.toString() == year }
        ?.let { Color(it.color) } ?: QuantumCyan
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = year, fontSize = 12.sp, color = accent, fontFamily = SpaceGroteskFont)
        Text(
            text = multiple,
            fontFamily = SyneFont,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TapeChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF140D2E))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 10.sp, color = NeonAmber)
        Text(
            text = value,
            fontFamily = JetBrainsMonoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
