package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.rainbow.RainbowCycleSection
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SpaceGroteskFont
import com.example.ui.theme.TachyonMint
import com.example.util.CycleFractalData
import com.example.util.LocalAppStrings

@Suppress("UNUSED_PARAMETER")
@Composable
fun CycleRainbowHome(
    reading: CycleFractalData?,
    priceUsd: Double,
    priceIsLive: Boolean,
    isProUnlocked: Boolean,
    greek: Boolean,
    onOpenProModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isProUnlocked) {
        RainbowCycleSection(
            greek = greek,
            liveUsd = priceUsd,
            priceIsLive = priceIsLive,
            modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)
        )
    } else {
        val strings = LocalAppStrings.current
        val palette = LocalAppColors.current

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0F1C)),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(QuantumCyan.copy(alpha = 0.5f), Color(0xFF3B82F6).copy(alpha = 0.2f))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(QuantumCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = QuantumCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = strings.rainbowProLockedBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuantumCyan,
                        letterSpacing = 1.sp
                    )
                }

                // Title & Description
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = strings.rainbowProLockedTitle,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGroteskFont,
                        color = Color.White
                    )
                    Text(
                        text = strings.rainbowProLockedDesc,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                // Features list
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF141729))
                        .padding(14.dp)
                ) {
                    RainbowFeatureItem(text = strings.rainbowProFeature1)
                    RainbowFeatureItem(text = strings.rainbowProFeature2)
                    RainbowFeatureItem(text = strings.rainbowProFeature3)
                }

                // Unlock Button
                Button(
                    onClick = onOpenProModal,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QuantumCyan,
                        contentColor = Color(0xFF070B14)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = strings.rainbowProUnlockBtn,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RainbowFeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(QuantumCyan)
        )
        Text(
            text = text,
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            color = Color(0xFFE5E7EB),
            fontWeight = FontWeight.Medium
        )
    }
}
