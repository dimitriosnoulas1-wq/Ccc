package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billing.BillingManager
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProUpgradeModal(
    isProUnlocked: Boolean,
    monthlyPrice: String = "€3.99",
    yearlyPrice: String = "€34.99",
    onDismiss: () -> Unit,
    onPurchaseMonthly: () -> Unit,
    onPurchaseYearly: () -> Unit,
    onRestorePurchases: ((isSuccess: Boolean, message: String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var selectedPlan by remember { mutableStateOf(BillingManager.PRODUCT_PRO_MONTHLY) }
    var isRestoring by remember { mutableStateOf(false) }
    var restoreFeedbackMessage by remember { mutableStateOf<String?>(null) }

    fun safeDismiss() {
        coroutineScope.launch {
            try {
                sheetState.hide()
            } catch (_: Exception) {
            } finally {
                onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CosmicVoidBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CosmicBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .verticalScroll(rememberScrollState())
                .testTag("pro_upgrade_modal"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar with Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isProUnlocked) TachyonMint.copy(alpha = 0.2f) else PhotonGold.copy(alpha = 0.2f))
                            .border(1.dp, if (isProUnlocked) TachyonMint else PhotonGold, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isProUnlocked) strings.proActive else "GOOGLE PLAY BILLING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isProUnlocked) TachyonMint else PhotonGold
                        )
                    }
                }

                IconButton(
                    onClick = {
                        com.example.util.AppSoundManager.playTechClick()
                        safeDismiss()
                    },
                    modifier = Modifier.testTag("close_pro_modal")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.closeButton,
                        tint = TextSecondary
                    )
                }
            }

            // Glowing Pro Icon Header
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(PhotonGold.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
                    .border(1.5.dp, PhotonGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Pro",
                    tint = PhotonGold,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = strings.proModalTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = strings.proModalSubtitle,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pro Features Checklist Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                ProFeatureRow(strings.proModalFeature1)
                ProFeatureRow(strings.proModalFeature2)
                ProFeatureRow(strings.proModalFeature3)
                ProFeatureRow(strings.proModalFeature4)
                ProFeatureRow(strings.proModalFeature5)
                ProFeatureRow(strings.proModalFeature6)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Plan Selectors: Monthly (with 7-Day Trial) vs Yearly
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Monthly with 7-Day Free Trial
                PlanOptionCard(
                    title = strings.planMonthlyTitle,
                    price = monthlyPrice,
                    period = strings.planMonthlyPeriod,
                    badge = strings.planMonthlyBadge,
                    badgeColor = QuantumCyan,
                    isSelected = selectedPlan == BillingManager.PRODUCT_PRO_MONTHLY,
                    onClick = {
                        com.example.util.AppSoundManager.playTechClick()
                        selectedPlan = BillingManager.PRODUCT_PRO_MONTHLY
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("plan_monthly_selector")
                )

                // Yearly
                PlanOptionCard(
                    title = strings.planAnnualTitle,
                    price = yearlyPrice,
                    period = strings.planAnnualPeriod,
                    badge = strings.planAnnualBadge,
                    badgeColor = PhotonGold,
                    isSelected = selectedPlan == BillingManager.PRODUCT_PRO_YEARLY,
                    onClick = {
                        com.example.util.AppSoundManager.playTechClick()
                        selectedPlan = BillingManager.PRODUCT_PRO_YEARLY
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("plan_yearly_selector")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isProUnlocked) {
                // Active Pro status indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TachyonMint.copy(alpha = 0.15f))
                        .border(1.dp, TachyonMint, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = TachyonMint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pro Membership is currently ACTIVE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TachyonMint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        com.example.util.AppSoundManager.playTechClick()
                        try {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://play.google.com/store/account/subscriptions")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Manage via Google Play Store > Subscriptions", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Manage in Google Play Store",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Main Google Play Purchase / Trial CTA
                Button(
                    onClick = {
                        com.example.util.AppSoundManager.playTechClick()
                        if (selectedPlan == BillingManager.PRODUCT_PRO_MONTHLY) {
                            onPurchaseMonthly()
                        } else {
                            onPurchaseYearly()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedPlan == BillingManager.PRODUCT_PRO_MONTHLY) QuantumCyan else PhotonGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("subscribe_pro_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = Color(0xFF05050F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedPlan == BillingManager.PRODUCT_PRO_MONTHLY) {
                                strings.startFreeTrialBtn
                            } else {
                                strings.continueYearlyBtn
                            },
                            color = Color(0xFF05050F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Subtext for selected plan
                Text(
                    text = if (selectedPlan == BillingManager.PRODUCT_PRO_MONTHLY) {
                        strings.startFreeTrialSub
                            .replace("€3.99", monthlyPrice)
                            .replace("3,99 €", monthlyPrice)
                            .replace("3.99 €", monthlyPrice)
                    } else {
                        strings.continueYearlySub
                            .replace("€34.99", yearlyPrice)
                            .replace("34,99 €", yearlyPrice)
                            .replace("34.99 €", yearlyPrice)
                            .replace("€3.99", yearlyPrice)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Restore Purchases Button
            OutlinedButton(
                onClick = {
                    com.example.util.AppSoundManager.playTechClick()
                    isRestoring = true
                    restoreFeedbackMessage = null
                    onRestorePurchases { success, message ->
                        isRestoring = false
                        restoreFeedbackMessage = message
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("restore_purchases_button")
            ) {
                if (isRestoring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = QuantumCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.restoringPurchases,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = strings.restorePurchases,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.restorePurchases,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }

            if (restoreFeedbackMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = restoreFeedbackMessage.orEmpty(),
                    fontSize = 11.sp,
                    color = QuantumCyan,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Play Subscription Terms Disclosure
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Google Play",
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Google Play Subscriptions",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.playTermsDisclosure,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mandatory Financial & Regulatory Disclaimer Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Disclaimer",
                        tint = PhotonGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.financialDisclaimerTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PhotonGold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.financialDisclaimerText,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun ProFeatureRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(TachyonMint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Included",
                tint = TachyonMint,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = TextPrimary
        )
    }
}

@Composable
fun PlanOptionCard(
    title: String,
    price: String,
    period: String,
    badge: String?,
    badgeColor: Color = PhotonGold,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) CosmicVoidSurfaceElevated else CosmicVoidSurface
            )
            .border(
                1.5.dp,
                if (isSelected) badgeColor else CosmicBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            badge?.let {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF05050F)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                text = title,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = price,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = period,
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                )
            }
        }
    }
}
