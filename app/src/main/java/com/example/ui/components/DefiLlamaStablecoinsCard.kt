package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StablecoinLiquidityData
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.holographicCard
import java.util.Locale
import kotlin.math.abs

@Composable
fun DefiLlamaStablecoinsCard(
    liquidityData: StablecoinLiquidityData = StablecoinLiquidityData(),
    isGreek: Boolean
) {
    val palette = LocalAppColors.current

    val totalBillion = liquidityData.totalCirculatingUsd / 1_000_000_000.0
    val changeBillion = liquidityData.change7dUsd / 1_000_000_000.0
    val changePct = liquidityData.change7dPercent

    val isExpanding = changeBillion >= 0.0
    val flowColor = if (isExpanding) Color(0xFF00E676) else Color(0xFFFF5252)

    val flowText = String.format(
        Locale.US,
        "%s$%.2fB (%s%.2f%%)",
        if (isExpanding) "+" else "-",
        abs(changeBillion),
        if (isExpanding) "+" else "-",
        abs(changePct)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .holographicCard(
                shape = RoundedCornerShape(16.dp),
                glowColor = QuantumCyan
            )
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = QuantumCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isGreek) "Global Stablecoin Supply & 7D Flow" else "Global Stablecoin Supply & 7D Flow",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            }

            Box(
                modifier = Modifier
                    .holographicCard(
                        shape = RoundedCornerShape(6.dp),
                        glowColor = flowColor
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(flowColor, CircleShape)
                    )
                    Text(
                        text = if (!liquidityData.isLive) {
                            if (isGreek) "Offline" else "Offline"
                        } else if (isGreek) {
                            if (isExpanding) "Επέκταση Ρευστότητας" else "Συρρίκνωση Ρευστότητας"
                        } else {
                            if (isExpanding) "Liquidity Inflow" else "Liquidity Outflow"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = flowColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isGreek) "Συνολική Προσφορά (USDT + USDC)" else "Total Circulating (USDT+USDC)",
                    fontSize = 11.sp,
                    color = palette.textMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = String.format(Locale.US, "$%.2fB", totalBillion),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = palette.textPrimary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isGreek) "7D Καθαρή Ροή (Net Flow)" else "7D Net Liquidity Flow",
                    fontSize = 11.sp,
                    color = palette.textMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = flowText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = flowColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = palette.textMuted,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (isGreek) "Πηγή: ${liquidityData.sourceName}" else "Source: ${liquidityData.sourceName}",
                    fontSize = 10.sp,
                    color = palette.textMuted,
                    maxLines = 1
                )
            }

            Text(
                text = String.format(Locale.US, "USDT Dom: %.1f%%", liquidityData.usdtDominancePercent),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = QuantumCyan
            )
        }
    }
}
