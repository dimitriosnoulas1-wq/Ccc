package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.util.LocalAppStrings

@Composable
fun SignalsProGatekeeper(
    onOpenProModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalAppColors.current

    val isGreek = strings.language.code == "el"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(palette.surface)
            .border(1.dp, palette.border, RoundedCornerShape(10.dp))
            .clickable { onOpenProModal() }
            .padding(16.dp)
            .testTag("signals_pro_gatekeeper")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isGreek) "Μηχανή Σημάτων & Αναλύσεις Κύκλου" else "Signals Engine & Fractal Analytics",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary
                )
                Text(
                    text = if (isGreek) "Ξεκλείδωμα Pro →" else "Unlock Pro →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.primary
                )
            }

            Text(
                text = if (isGreek)
                    "Πρόσβαση σε live καμπύλες halving, παλινδρομήσεις κύκλων 2020 & 2016 και σήματα ρευστοποιήσεων υψηλής συχνότητας."
                else
                    "Access live institutional halving analog fractal curves, 2020 & 2016 cycle regressions, and high-frequency liquidation signals.",
                fontSize = 12.sp,
                color = palette.textSecondary,
                lineHeight = 17.sp
            )

            // 90D Verified Backtest Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.primary.copy(alpha = 0.1f))
                    .border(1.dp, palette.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isGreek) "Live whale radar · live order flow · live liquidations" else "Live whale radar · live order flow · live liquidations",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isGreek) "4.79 € / μήνα · 7 ημέρες δωρεάν δοκιμή" else "€4.79 / month · 7-Day Free Trial",
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
                Text(
                    text = if (isGreek) "Προβολή λεπτομερειών" else "View details",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.primary
                )
            }
        }
    }
}
