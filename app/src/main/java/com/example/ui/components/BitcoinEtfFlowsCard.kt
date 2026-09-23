package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
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
import com.example.data.model.BitcoinEtfFlowData
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.holographicCard
import java.util.Locale
import kotlin.math.abs

@Composable
fun BitcoinEtfFlowsCard(
    etfFlowData: BitcoinEtfFlowData = BitcoinEtfFlowData(),
    isGreek: Boolean
) {
    val palette = LocalAppColors.current

    val oneDayFlow = etfFlowData.oneDayNetFlowMillionUsd
    val fiveDayFlow = etfFlowData.fiveDayCumulativeMillionUsd

    val is1dPositive = oneDayFlow >= 0.0
    val color1d = if (is1dPositive) Color(0xFF00E676) else Color(0xFFFF5252)
    val text1d = String.format(Locale.US, "%s$%.1fM", if (is1dPositive) "+" else "-", abs(oneDayFlow))

    val is5dPositive = fiveDayFlow >= 0.0
    val color5d = if (is5dPositive) Color(0xFF00E676) else Color(0xFFFF5252)
    val text5d = String.format(Locale.US, "%s$%.1fM", if (is5dPositive) "+" else "-", abs(fiveDayFlow))

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
                    imageVector = Icons.AutoMirrored.Outlined.ShowChart,
                    contentDescription = null,
                    tint = QuantumCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isGreek) "US Spot BTC ETF (1D / 5D)" else "US Spot BTC ETF Net Flows",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            }
            
            Box(
                modifier = Modifier
                    .holographicCard(
                        shape = RoundedCornerShape(6.dp),
                        glowColor = if (is1dPositive) Color(0xFF00E676) else Color(0xFFFF5252)
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
                            .background(
                                color = if (is1dPositive) Color(0xFF00E676) else Color(0xFFFF5252),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = if (isGreek) {
                            if (is1dPositive) "Εισροές / Live Feed" else "Εκροές / Live Feed"
                        } else {
                            if (is1dPositive) "Inflows / Live" else "Outflows / Live"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (is1dPositive) Color(0xFF00E676) else Color(0xFFFF5252)
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
                    text = if (isGreek) "1D Net Flow (Ημερήσια)" else "1D Net Flow",
                    fontSize = 11.sp,
                    color = palette.textMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = text1d,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = color1d
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isGreek) "5D Cumulative (Σωρευτικά)" else "5D Cumulative",
                    fontSize = 11.sp,
                    color = palette.textMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = text5d,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = color5d
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Settlement Notice
        Text(
            text = if (isGreek) "Το ημερήσιο κλείσιμο ενημερώνεται μετά το πέρας των συναλλαγών στις ΗΠΑ (Daily closing tape updated after US market settlement)." else "Daily closing tape updated after US market settlement.",
            fontSize = 9.sp,
            lineHeight = 13.sp,
            color = palette.textMuted
        )

        Spacer(modifier = Modifier.height(6.dp))

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
                    text = if (isGreek) "Πηγή: ${etfFlowData.sourceName}" else "Source: ${etfFlowData.sourceName}",
                    fontSize = 10.sp,
                    color = palette.textMuted,
                    maxLines = 1
                )
            }

            Text(
                text = if (isGreek) "Ημερομηνία: ${etfFlowData.asOfDate}" else "As of: ${etfFlowData.asOfDate}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = QuantumCyan
            )
        }
    }
}
