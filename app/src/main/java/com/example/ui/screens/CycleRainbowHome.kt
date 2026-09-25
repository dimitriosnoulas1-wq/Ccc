package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.rainbow.RainbowCycleSection
import com.example.util.CycleFractalData

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
    RainbowCycleSection(
        greek = greek,
        liveUsd = priceUsd,
        priceIsLive = priceIsLive,
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)
    )
}
