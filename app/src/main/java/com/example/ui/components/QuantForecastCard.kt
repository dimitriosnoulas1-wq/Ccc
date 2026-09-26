package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.forecasting.ForecastCardModel
import com.example.engine.forecasting.ForecastDirection
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.TachyonMint
import com.example.util.LocalAppStrings

@Composable
fun QuantForecastCard(
    model: ForecastCardModel,
    isProUser: Boolean,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var showAdvancedDetails by remember { mutableStateOf(false) }

    val regimeColor = when (model.direction) {
        ForecastDirection.BULLISH -> TachyonMint
        ForecastDirection.BEARISH -> SoftCrimson
        else -> NeonAmber
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CosmicVoidSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(regimeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${model.asset} TAPE READING",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "DAILY TREND (EMA20/50, RSI)",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = model.regime.label,
                        color = regimeColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "COMPOSITE SCORE",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${if (model.compositeScore > 0) "+" else ""}${model.compositeScore} / 100",
                        color = regimeColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicVoidSurfaceElevated)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "💡 MARKET SUMMARY",
                        color = QuantumCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = model.simpleExplanation,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdvancedDetails = !showAdvancedDetails }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showAdvancedDetails) "Hide inputs ▲" else "Why? Show the inputs ▼",
                    color = QuantumCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = showAdvancedDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    model.technicalEvidences.forEach { evidence ->
                        Text(
                            text = "• $evidence",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Key Support: ${moneyOrDash(model.keySupport)}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(text = "Resistance: ${moneyOrDash(model.keyResistance)}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Text(
                        text = "Lowest print: ${moneyOrDash(model.invalidationLevel)}",
                        color = SoftCrimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⚠️ ${model.riskWarning}",
                        color = QuantumCyan,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Live reading of daily closes, funding, and ETF when those feeds are up. Not a forecast and not a trade.",
                color = Color(0xFF64748B),
                fontSize = 9.sp,
                lineHeight = 12.sp
            )
        }

        if (!isProUser) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CosmicVoidBg.copy(alpha = 0.90f))
                    .clickable { onUpgradeClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔒 ${strings.proTapeTitle}",
                        color = QuantumCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.proTapeDesc,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onUpgradeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = QuantumCyan)
                    ) {
                        Text(text = strings.upgradeToPro.uppercase(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun moneyOrDash(value: Double): String =
    if (value > 0.0) "$${String.format(java.util.Locale.US, "%.2f", value)}" else "—"
