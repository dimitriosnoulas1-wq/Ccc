package com.example.ui.components

import androidx.compose.runtime.staticCompositionLocalOf

/** Prices and trial length as Google Play returned them; blank until Play answers. */
data class PaywallPrices(val monthly: String = "", val yearly: String = "", val trialDays: Int? = null)

val LocalPaywallPrices = staticCompositionLocalOf { PaywallPrices() }
