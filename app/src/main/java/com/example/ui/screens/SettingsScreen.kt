package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeOption
import com.example.data.model.Currency
import com.example.data.model.WhaleAlertSettings
import com.example.ui.theme.AppThemePalette
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.util.AppSoundManager
import com.example.util.LocalAppStrings

@Composable
fun SettingsScreen(
    currency: Currency,
    selectedLanguage: AppLanguage,
    isProUnlocked: Boolean,
    monthlyPrice: String = "4.79 €",
    yearlyPrice: String = "19.99 €",
    whaleSettings: WhaleAlertSettings = WhaleAlertSettings(),
    btcPrice: Double = 0.0,
    onCurrencyChanged: (Currency) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onOpenProModal: () -> Unit,
    onRestorePurchases: (((Boolean, String) -> Unit) -> Unit)? = null,
    onOpenPrivacyPolicy: () -> Unit,
    onWhaleNotificationsChanged: (Boolean) -> Unit = {},
    onWhaleThresholdChanged: (Double) -> Unit = {},
    onNotifyZoneChangeChanged: (Boolean) -> Unit = {},
    onNotifyPiCycleChanged: (Boolean) -> Unit = {},
    onNotifyRainbowBandChanged: (Boolean) -> Unit = {},
    onNotify200wSmaChanged: (Boolean) -> Unit = {},
    logCharts: Boolean = true,
    onLogChartsChanged: (Boolean) -> Unit = {},
    onTogglePro: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalAppColors.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var isRestoringSettings by remember { mutableStateOf(false) }
    var developerTapCount by remember { mutableStateOf(0) }

    var pendingPermissionCallback by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            val cb = pendingPermissionCallback
            pendingPermissionCallback = null
            cb?.invoke(isGranted)
        }
    )

    val requestNotificationPermissionIfNecessary = remember(context) {
        { onGranted: () -> Unit ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (com.example.util.NotificationHelper.canSendNotifications(context)) {
                    onGranted()
                } else {
                    pendingPermissionCallback = { isGranted ->
                        if (isGranted) {
                            onGranted()
                        }
                    }
                    try {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } catch (_: Throwable) {
                        // Gracefully handle launcher exceptions
                    }
                }
            } else {
                onGranted()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .border(1.dp, palette.primary.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RadioButtonChecked,
                            contentDescription = "Logo",
                            tint = palette.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.appTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (palette.isMonochrome) palette.border else if (isProUnlocked) NeonAmber else palette.primary)
                        .clickable {
                            com.example.util.AppSoundManager.playTechClick()
                            onOpenProModal()
                        }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                        .testTag("settings_top_pro_badge")
                ) {
                    Text(
                        text = if (isProUnlocked) strings.proActive else strings.proBadge,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (palette.isMonochrome) palette.textPrimary else Color(0xFF05050F)
                    )
                }
            }
        }

        item {
            Column {
                Text(
                    text = strings.settingsHeader,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = palette.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.languageTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.languageDesc,
                    fontSize = 13.sp,
                    color = palette.textSecondary
                )
            }
        }

        // Language Grid (2 columns)
        item {
            val languages = AppLanguage.values()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in languages.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageCard(
                            language = languages[i],
                            isSelected = selectedLanguage == languages[i],
                            onSelect = { onLanguageChanged(languages[i]) },
                            modifier = Modifier.weight(1f)
                        )
                        if (i + 1 < languages.size) {
                            LanguageCard(
                                language = languages[i + 1],
                                isSelected = selectedLanguage == languages[i + 1],
                                onSelect = { onLanguageChanged(languages[i + 1]) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Options (Log charts & Futuristic Sound Effects with Holographic Toggles)
        item {
            val isSoundEnabled by com.example.util.AppSoundManager.isSoundEnabled.collectAsState()

            SettingsCard(
                title = strings.optionsTitle,
                icon = Icons.Default.Tune
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Futuristic Audio FX Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.soundEffectsTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.textPrimary
                            )
                            Text(
                                text = strings.soundEffectsSub,
                                fontSize = 11.sp,
                                color = palette.textMuted
                            )
                        }
                        HolographicToggle(
                            checked = isSoundEnabled,
                            onCheckedChange = {
                                AppSoundManager.setSoundEnabled(it)
                            },
                            activeColor = NeonCyan
                        )
                    }

                    if (isSoundEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.surfaceElevated.copy(alpha = 0.5f))
                                .border(1.dp, palette.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = strings.soundTestTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(palette.surface)
                                        .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                                        .clickable { com.example.util.AppSoundManager.playLaunchAmbient() }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = strings.soundTestLaunch,
                                        fontSize = 11.sp,
                                        color = palette.textPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(palette.surface)
                                        .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                                        .clickable { com.example.util.AppSoundManager.playTechClick() }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = strings.soundTestClick,
                                        fontSize = 11.sp,
                                        color = palette.textPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(palette.surface)
                                        .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                                        .clickable { com.example.util.AppSoundManager.playSwitchToggle(true) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = strings.soundTestToggle,
                                        fontSize = 11.sp,
                                        color = palette.textPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(palette.surface)
                                        .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                                        .clickable { com.example.util.AppSoundManager.playSuccessChime() }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = strings.soundTestChime,
                                        fontSize = 11.sp,
                                        color = palette.textPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Logarithmic Charts Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.logChartsTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.textPrimary
                            )
                            Text(
                                text = strings.logChartsSub,
                                fontSize = 11.sp,
                                color = palette.textMuted
                            )
                        }
                        HolographicToggle(
                            checked = logCharts,
                            onCheckedChange = onLogChartsChanged,
                            activeColor = palette.primary
                        )
                    }
                }
            }
        }

        // Membership / Pro Tier
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (palette.isMonochrome) {
                            Brush.verticalGradient(listOf(palette.surfaceElevated, palette.surface))
                        } else if (palette.isLight) {
                            Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)))
                        } else {
                            Brush.linearGradient(listOf(palette.surfaceElevated, palette.surface))
                        }
                    )
                    .border(1.dp, if (palette.isMonochrome) palette.border else if (isProUnlocked) NeonAmber else palette.border, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Pro",
                                tint = if (palette.isMonochrome) palette.textPrimary else NeonAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.proMembershipTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isProUnlocked) palette.gainColor.copy(alpha = 0.2f) else palette.surfaceElevated)
                                .border(1.dp, if (isProUnlocked) palette.gainColor else palette.border, RoundedCornerShape(8.dp))
                                .clickable {
                                    com.example.util.AppSoundManager.playTechClick()
                                    onOpenProModal()
                                }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isProUnlocked) strings.proActive else "4.79 € / mo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isProUnlocked) palette.gainColor else palette.primary
                            )
                        }
                    }

                    Text(
                        text = "$monthlyPrice ${strings.planMonthlyPeriod} (${strings.planMonthlyBadge}) · $yearlyPrice ${strings.planAnnualPeriod}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.textSecondary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProBenefitRow(strings.proBenefit1)
                        ProBenefitRow(strings.proBenefit2)
                        ProBenefitRow(strings.proBenefit3)
                        ProBenefitRow(strings.proBenefit4)
                        ProBenefitRow(strings.proBenefit5)
                    }

                    if (onTogglePro != null && developerTapCount >= 7) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (palette.isLight) Color(0xFFF1F5F9) else palette.surface)
                                .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isProUnlocked) palette.gainColor else NeonAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isProUnlocked) "Pro Status: UNLOCKED" else "Pro Status: LOCKED",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isProUnlocked) palette.gainColor else palette.textPrimary
                                )
                            }
                            HolographicToggle(
                                checked = isProUnlocked,
                                onCheckedChange = {
                                    onTogglePro(it)
                                },
                                activeColor = palette.gainColor,
                                modifier = Modifier.testTag("pro_membership_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            com.example.util.AppSoundManager.playTechClick()
                            onOpenProModal()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isProUnlocked) palette.surfaceElevated else palette.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_upgrade_pro_button")
                    ) {
                        Text(
                            text = if (isProUnlocked) "Manage Pro Subscription" else "${strings.upgradeToPro} (7-Day Trial)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isProUnlocked) palette.textPrimary else (if (palette.isLight) Color.White else Color(0xFF05050F))
                        )
                    }

                    // Restore Purchases Button in Settings
                    if (onRestorePurchases != null) {
                        OutlinedButton(
                            onClick = {
                                com.example.util.AppSoundManager.playTechClick()
                                isRestoringSettings = true
                                onRestorePurchases { success, message ->
                                    isRestoringSettings = false
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("settings_restore_pro_button")
                        ) {
                            Text(
                                text = if (isRestoringSettings) strings.restoringPurchases else strings.restorePurchases,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Pro Whale Alert Notifications Settings Card
        item {
            SettingsCard(
                title = strings.whaleNotificationsTitle,
                icon = Icons.Default.NotificationsActive
            ) {
                if (!isProUnlocked) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.whaleNotificationsProLockedDesc,
                                fontSize = 12.sp,
                                color = palette.textMuted,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = onOpenProModal,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonAmber,
                                contentColor = Color(0xFF05050F)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.upgradeToPro,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF05050F)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.whaleNotificationsTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = strings.whaleNotificationsDesc,
                                    fontSize = 11.sp,
                                    color = palette.textMuted
                                )
                            }
                            HolographicToggle(
                                checked = whaleSettings.notificationsEnabled,
                                onCheckedChange = { isEnabled ->
                                    if (isEnabled) {
                                        requestNotificationPermissionIfNecessary {
                                            onWhaleNotificationsChanged(true)
                                        }
                                    } else {
                                        onWhaleNotificationsChanged(false)
                                    }
                                },
                                activeColor = NeonAmber
                            )
                        }

                        // Threshold Selector
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = strings.whaleThresholdTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.textSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    50_000_000.0 to "$50M",
                                    100_000_000.0 to "$100M",
                                    250_000_000.0 to "$250M",
                                    500_000_000.0 to "$500M"
                                ).forEach { (threshold, label) ->
                                    val isSelected = whaleSettings.minThresholdUsd == threshold
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) NeonAmber else palette.surfaceElevated
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) NeonAmber else palette.border,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onWhaleThresholdChanged(threshold) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF05050F) else palette.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        // Cycle & Indicator Push Alerts (Zone change, Pi Cycle, Rainbow, 200W SMA)
        item {
            val activeCycleAlertsCount = listOf(
                whaleSettings.notifyZoneChange,
                whaleSettings.notifyPiCycle,
                whaleSettings.notifyRainbowBand,
                whaleSettings.notify200wSma
            ).count { it }

            SettingsCard(
                title = strings.alertSettingsHeader,
                icon = Icons.Default.Timeline
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = strings.alertSettingsSub,
                        fontSize = 12.sp,
                        color = palette.textMuted
                    )

                    // Free vs Pro tier status badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isProUnlocked) palette.gainColor.copy(alpha = 0.12f) else NeonAmber.copy(alpha = 0.12f))
                            .border(1.dp, if (isProUnlocked) palette.gainColor.copy(alpha = 0.4f) else NeonAmber.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isProUnlocked) strings.alertProUnlockNotice else strings.alertFreeLimitNotice,
                                fontSize = 11.sp,
                                color = if (isProUnlocked) palette.gainColor else NeonAmber,
                                modifier = Modifier.weight(1f)
                            )
                            if (!isProUnlocked) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonAmber)
                                        .clickable { onOpenProModal() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = strings.upgradeToPro,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF05050F)
                                    )
                                }
                            }
                        }
                    }

                    // 1. Zone Change Alert
                    CycleAlertToggleRow(
                        title = strings.alertZoneChangeTitle,
                        desc = strings.alertZoneChangeDesc,
                        isChecked = whaleSettings.notifyZoneChange,
                        onCheckedChange = { isChecked ->
                            if (!isProUnlocked && activeCycleAlertsCount >= 2 && isChecked) {
                                onOpenProModal()
                            } else if (isChecked) {
                                requestNotificationPermissionIfNecessary {
                                    onNotifyZoneChangeChanged(true)
                                }
                            } else {
                                onNotifyZoneChangeChanged(false)
                            }
                        },
                        palette = palette
                    )

                    // 2. Pi Cycle Gap/Cross Alert
                    CycleAlertToggleRow(
                        title = strings.alertPiCycleTitle,
                        desc = strings.alertPiCycleDesc,
                        isChecked = whaleSettings.notifyPiCycle,
                        onCheckedChange = { isChecked ->
                            if (!isProUnlocked && activeCycleAlertsCount >= 2 && isChecked) {
                                onOpenProModal()
                            } else if (isChecked) {
                                requestNotificationPermissionIfNecessary {
                                    onNotifyPiCycleChanged(true)
                                }
                            } else {
                                onNotifyPiCycleChanged(false)
                            }
                        },
                        palette = palette
                    )

                    // 3. Rainbow Band Alert (Pro-preferred)
                    CycleAlertToggleRow(
                        title = strings.alertRainbowTitle,
                        desc = strings.alertRainbowDesc,
                        isChecked = whaleSettings.notifyRainbowBand,
                        isProGated = !isProUnlocked && activeCycleAlertsCount >= 2,
                        onCheckedChange = { isChecked ->
                            if (!isProUnlocked && activeCycleAlertsCount >= 2 && isChecked) {
                                onOpenProModal()
                            } else if (isChecked) {
                                requestNotificationPermissionIfNecessary {
                                    onNotifyRainbowBandChanged(true)
                                }
                            } else {
                                onNotifyRainbowBandChanged(false)
                            }
                        },
                        palette = palette
                    )

                    // 4. Distance to 200W SMA Alert
                    CycleAlertToggleRow(
                        title = strings.alert200WTitle,
                        desc = strings.alert200WDesc,
                        isChecked = whaleSettings.notify200wSma,
                        isProGated = !isProUnlocked && activeCycleAlertsCount >= 2,
                        onCheckedChange = { isChecked ->
                            if (!isProUnlocked && activeCycleAlertsCount >= 2 && isChecked) {
                                onOpenProModal()
                            } else if (isChecked) {
                                requestNotificationPermissionIfNecessary {
                                    onNotify200wSmaChanged(true)
                                }
                            } else {
                                onNotify200wSmaChanged(false)
                            }
                        },
                        palette = palette
                    )
                }
            }
        }



        // Currency Selector
        item {
            SettingsCard(
                title = strings.currencyDisplayTitle,
                icon = Icons.Default.CurrencyExchange
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Currency.values().forEach { curr ->
                        val isSelected = currency == curr
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) palette.primary else palette.surfaceElevated
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) palette.primary else palette.border,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onCurrencyChanged(curr) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${curr.symbol} ${curr.code}",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) (if (palette.isLight) Color.White else Color(0xFF05050F)) else palette.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // How we read it — in plain words
        item {
            SettingsCard(
                title = strings.howWeReadItTitle,
                icon = Icons.Default.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlainWordItem(
                        title = strings.plainItem1Title,
                        description = strings.plainItem1Desc
                    )
                    PlainWordItem(
                        title = strings.plainItem2Title,
                        description = strings.plainItem2Desc
                    )
                    PlainWordItem(
                        title = strings.plainItem3Title,
                        description = strings.plainItem3Desc
                    )
                    PlainWordItem(
                        title = strings.plainItem4Title,
                        description = strings.plainItem4Desc
                    )
                    PlainWordItem(
                        title = strings.plainItem5Title,
                        description = strings.plainItem5Desc
                    )
                }
            }
        }

        // Privacy Policy link
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.border, RoundedCornerShape(16.dp))
                    .clickable { 
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://cryptocycles.app/privacy"))
                        try {
                            context.startActivity(intent)
                        } catch (_: Throwable) {
                            onOpenPrivacyPolicy()
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(palette.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Privacy",
                            tint = palette.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = strings.privacyPolicyTitle,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary
                    )
                }
                Text(
                    text = strings.privacyPolicyView,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary
                )
            }
        }

        // Mandatory Financial Disclaimer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (palette.isLight) Color(0xFFFFFBEB) else palette.surface)
                    .border(1.dp, if (palette.isLight) Color(0xFFFDE68A) else palette.border, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Disclaimer",
                            tint = NeonAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.financialDisclaimerTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber
                        )
                    }
                    Text(
                        text = strings.financialDisclaimerText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = palette.textSecondary
                    )
                }
            }
        }

        // App Version & Build info + Developer Mode Pro Switch
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CryptoCycles v${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary,
                    modifier = Modifier.clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        developerTapCount++
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Google Play Internal Testing Ready • Production Release",
                    fontSize = 10.sp,
                    color = palette.textMuted
                )

                if (onTogglePro != null && developerTapCount >= 7) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (palette.isLight) Color(0xFFF1F5F9) else palette.surface)
                            .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "⚡ Internal Testing Pro Toggle",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isProUnlocked) palette.gainColor else palette.primary
                                )
                                Text(
                                    text = if (isProUnlocked) "Pro is currently UNLOCKED" else "Pro is currently LOCKED",
                                    fontSize = 10.sp,
                                    color = palette.textSecondary
                                )
                            }
                            HolographicToggle(
                                checked = isProUnlocked,
                                onCheckedChange = { onTogglePro(it) },
                                activeColor = palette.gainColor
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LanguageCard(
    language: AppLanguage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current

    val neonGoldBorder = Brush.horizontalGradient(
        listOf(
            NeonAmber,
            Color(0xFF00F5FF),
            NeonAmber
        )
    )

    val cardBackground = if (isSelected) {
        Brush.linearGradient(
            listOf(
                Color(0xFF0A2428).copy(alpha = 0.92f),
                Color(0xFF05050F).copy(alpha = 0.95f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                CosmicVoidSurface.copy(alpha = 0.85f),
                CosmicVoidBg.copy(alpha = 0.90f)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardBackground)
            .border(
                if (isSelected) 1.2.dp else 1.dp,
                if (isSelected) neonGoldBorder else Brush.verticalGradient(listOf(CosmicBorder, CosmicVoidBg)),
                RoundedCornerShape(14.dp)
            )
            .clickable {
                AppSoundManager.playTechClick()
                onSelect()
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.nativeName,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) NeonAmber else palette.textPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = language.englishName,
                    fontSize = 10.5.sp,
                    color = if (isSelected) Color(0xFF00F5FF).copy(alpha = 0.75f) else palette.textMuted,
                    maxLines = 1
                )
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.7f, animationSpec = spring(dampingRatio = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF7DF9FF),
                                    NeonAmber
                                )
                            )
                        )
                        .border(1.dp, Color(0xFFE9FEFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color(0xFF05050F),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HolographicToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = NeonAmber,
    enabled: Boolean = true
) {
    val orbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 3.dp,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "orbOffset"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(250),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .size(width = 50.dp, height = 28.dp)
            .clip(CircleShape)
            .background(
                if (checked) {
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF091424),
                            activeColor.copy(alpha = 0.35f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0B121C),
                            Color(0xFF060B12)
                        )
                    )
                }
            )
            .border(
                1.2.dp,
                if (checked) {
                    Brush.horizontalGradient(
                        listOf(
                            activeColor.copy(alpha = 0.6f),
                            activeColor
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF334155),
                            Color(0xFF101C2B)
                        )
                    )
                },
                CircleShape
            )
            .clickable(enabled = enabled) {
                AppSoundManager.playSwitchToggle(!checked)
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Inner track ambient glow when active
        if (checked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                activeColor.copy(alpha = 0.20f * glowAlpha)
                            )
                        )
                    )
            )
        }

        // Plasma Orb
        Box(
            modifier = Modifier
                .padding(start = orbOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    if (checked) {
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                activeColor,
                                activeColor.copy(alpha = 0.85f)
                            ),
                            radius = 28f
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6B7E99),
                                Color(0xFF2C3B4E),
                                Color(0xFF142030)
                            ),
                            radius = 28f
                        )
                    }
                )
                .border(
                    0.8.dp,
                    if (checked) Color.White.copy(alpha = 0.9f) else Color(0xFF425670),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Hot white plasma nucleus
            if (checked) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun ProBenefitRow(text: String) {
    val palette = LocalAppColors.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = palette.gainColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = palette.textSecondary
        )
    }
}

@Composable
private fun PlainWordItem(title: String, description: String) {
    val palette = LocalAppColors.current

    Column {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = palette.textSecondary
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val palette = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surface)
            .border(1.dp, palette.border, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = palette.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = palette.primary
            )
        }
        content()
    }
}

@Composable
fun CycleAlertToggleRow(
    title: String,
    desc: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    palette: AppThemePalette,
    isProGated: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary
                )
                if (isProGated) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "PRO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber
                        )
                    }
                }
            }
            Text(
                text = desc,
                fontSize = 11.sp,
                color = palette.textMuted
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        HolographicToggle(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            activeColor = if (isProGated) NeonAmber else palette.primary
        )
    }
}
