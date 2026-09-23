package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.PhotonGoldBright
import com.example.ui.theme.QuantumBlue
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.QuantumCyanBright
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.SoftEmerald
import com.example.ui.theme.TextCyanSlate
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.holographicCard
import com.example.util.LocalAppStrings

/**
 * Institutional Whale Risk Scanner & Exit Strategy Simulator.
 * 100% Anonymous & Zero-KYC: allows serious investors and whales to simulate
 * their portfolio exposure against current Halving Days, Pi Cycle Overheat, and Rainbow bands.
 */
@Composable
fun WhalePortfolioRiskSimulatorCard(
    isProUnlocked: Boolean,
    onOpenProModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalAppColors.current
    val isGreek = strings.language.code == "el"

    var isExpanded by remember { mutableStateOf(false) }

    // Allocations (Sliders)
    var btcShare by remember { mutableStateOf(50f) }
    var ethShare by remember { mutableStateOf(25f) }
    var altShare by remember { mutableStateOf(15f) }
    var stableShare by remember { mutableStateOf(10f) }

    val daysSinceHalving = com.example.util.HalvingCycleUtils.getDaysSince4thHalving()

    val totalWeight = btcShare + ethShare + altShare + stableShare
    val normBtc = if (totalWeight > 0) btcShare / totalWeight else 0.5f
    val normEth = if (totalWeight > 0) ethShare / totalWeight else 0.25f
    val normAlts = if (totalWeight > 0) altShare / totalWeight else 0.15f
    val normStables = if (totalWeight > 0) stableShare / totalWeight else 0.10f

    val calculatedRisk = ((normBtc * 58f) + (normEth * 66f) + (normAlts * 80f) + (normStables * 0f)).coerceIn(10f, 95f)

    val riskColor = when {
        calculatedRisk > 75f -> SoftCrimson // High risk / Overheat
        calculatedRisk > 55f -> PhotonGold // Moderate heat
        else -> SoftEmerald // Healthy
    }

    val recommendedExitDca = if (isGreek) {
        "Καμία εντολή εξόδου. Μόνο η ιστορική ημέρα κύκλου."
    } else {
        "No exit order. This is the historical cycle day only."
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .holographicCard(
                shape = RoundedCornerShape(16.dp),
                glowColor = QuantumCyan,
                pulseColor = PhotonGold,
                baseContainerColor = Color(0xDD0B132B)
            )
            .padding(14.dp)
            .testTag("whale_portfolio_risk_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(PhotonGold.copy(alpha = 0.22f), QuantumCyan.copy(alpha = 0.18f))
                                )
                            )
                            .border(1.dp, QuantumCyan.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Whale Security",
                            tint = QuantumCyanBright,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (isGreek) "Whale Exit & Risk Simulator" else "Whale Exit & Risk Simulator",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPureWhite
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF9C27B0).copy(alpha = 0.25f))
                                    .border(0.5.dp, Color(0xFFCE93D8).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "COLD PRIVACY",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFE1BEE7)
                                )
                            }
                        }
                        Text(
                            text = if (isGreek) "Ανώνυμη ανάλυση κινδύνου κεφαλαίου & έξοδος κορυφής" else "Zero-KYC capital preservation & exit bands scanner",
                            fontSize = 11.sp,
                            color = TextCyanSlate
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle",
                    tint = QuantumCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Quick Snapshot (Holographic Laser Grid)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xE6081024))
                    .border(1.dp, Color(0x3300D2FF), RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isGreek) "Εκτιμώμενο Ρίσκο Κύκλου" else "Estimated Cycle Risk",
                        fontSize = 9.5.sp,
                        color = TextCyanSlate
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${calculatedRisk.toInt()}/100",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = riskColor
                        )
                        Text(
                            text = when {
                                calculatedRisk > 75f -> if (isGreek) "ΥΨΗΛΟ ΡΙΣΚΟ" else "HIGH HEAT"
                                calculatedRisk > 55f -> if (isGreek) "ΜΕΤΡΙΟ ΡΙΣΚΟ" else "MODERATE"
                                else -> if (isGreek) "ΑΣΦΑΛΗΣ ΖΩΝΗ" else "SAFE"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isGreek) "Ημέρα Halving" else "Halving Day",
                        fontSize = 9.5.sp,
                        color = TextCyanSlate
                    )
                    Text(
                        text = "Day $daysSinceHalving",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = QuantumCyanBright
                    )
                }
            }

            // Expandable Interactive Simulator
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = if (isGreek) "Προσαρμόστε την κατανομή χαρτοφυλακίου (Zero-KYC Simulation):" else "Adjust portfolio allocations (Zero-KYC Simulation):",
                        fontSize = 11.5.sp,
                        color = TextCyanSlate
                    )

                    // Sliders
                    AllocationSlider(
                        label = "Bitcoin (BTC)",
                        percentage = (normBtc * 100).toInt(),
                        color = PhotonGold,
                        value = btcShare,
                        onValueChange = { btcShare = it }
                    )
                    AllocationSlider(
                        label = "Ethereum (ETH)",
                        percentage = (normEth * 100).toInt(),
                        color = QuantumBlue,
                        value = ethShare,
                        onValueChange = { ethShare = it }
                    )
                    AllocationSlider(
                        label = "Altcoins / High Beta",
                        percentage = (normAlts * 100).toInt(),
                        color = QuantumCyanBright,
                        value = altShare,
                        onValueChange = { altShare = it }
                    )
                    AllocationSlider(
                        label = "Stablecoins (USDT/USDC/Cash)",
                        percentage = (normStables * 100).toInt(),
                        color = SoftEmerald,
                        value = stableShare,
                        onValueChange = { stableShare = it }
                    )

                    // Whale Recommendation Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xD90E172E))
                            .border(1.dp, Color(0x3300D2FF), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.PieChart,
                                    contentDescription = null,
                                    tint = QuantumCyanBright,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isGreek) "Χωρίς εντολή θέσης" else "No position instruction",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPureWhite
                                )
                            }
                            Text(
                                text = recommendedExitDca,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = riskColor
                            )
                            Text(
                                text = if (isGreek)
                                    "Ημέρα $daysSinceHalving μετά το halving. Εκπαιδευτικό ιστορικό παρελθόντων κύκλων. Δεν είναι πρόβλεψη και δεν είναι συμβουλή."
                                else
                                    "Day $daysSinceHalving after the halving. Educational history of past cycles. Not a forecast and not advice.",
                                fontSize = 10.5.sp,
                                color = TextCyanSlate,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    if (!isProUnlocked) {
                        Button(
                            onClick = onOpenProModal,
                            colors = ButtonDefaults.buttonColors(containerColor = QuantumCyanBright),
                            modifier = Modifier.fillMaxWidth().testTag("whale_pro_upgrade_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isGreek) "Ξεκλείδωμα Whale Model Analytics (Pro)" else "Unlock Full Whale Model Analytics (Pro)",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllocationSlider(
    label: String,
    percentage: Int,
    color: Color,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Text(
                text = "$percentage%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color(0xFF263238)
            ),
            modifier = Modifier.height(26.dp)
        )
    }
}
