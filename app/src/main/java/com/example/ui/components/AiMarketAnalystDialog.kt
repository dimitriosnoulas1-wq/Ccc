package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AiChatMessage
import com.example.data.model.AiMessageSender
import com.example.ui.theme.CopperAccent
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicDarkBg
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.GainGreen
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.MarketRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocalAppStrings

@Composable
fun AiMarketAnalystDialog(
    messages: List<AiChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onDismiss: () -> Unit,
    aiQueriesRemaining: Int = 3,
    isAiLimitReached: Boolean = false,
    isProUnlocked: Boolean = false,
    onOpenProModal: () -> Unit = {}
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom automatically on new messages or loading change
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .imePadding()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(20.dp),
            color = palette.background,
            border = BorderStroke(1.dp, palette.border),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.surfaceElevated)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CopperAccent.copy(alpha = 0.15f))
                                .border(1.dp, CopperAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CopperAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = strings.aiAssistantTitle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GainGreen.copy(alpha = 0.15f))
                                        .border(0.5.dp, GainGreen, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = strings.aiAssistantBadge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GainGreen
                                    )
                                }
                            }

                            Text(
                                text = strings.aiAssistantSubtitle,
                                fontSize = 11.sp,
                                color = palette.textSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                com.example.util.AppSoundManager.playTechClick()
                                onClearChat()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = strings.aiClearHistory,
                                tint = palette.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                com.example.util.AppSoundManager.playTechClick()
                                onDismiss()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.closeButton,
                                tint = palette.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Live Context Tag Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.surface)
                        .border(0.5.dp, palette.border)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GainGreen)
                        )
                        Text(
                            text = strings.aiLiveContextTag,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textSecondary
                        )
                    }

                    Text(
                        text = "CryptoCycles Intelligence · Low Latency",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = palette.textMuted
                    )
                }

                // Chat Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatMessageItem(message = message)
                    }
                }

                // Quick Action Preset Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionChip(
                        label = strings.aiQuickActionCycle,
                        icon = Icons.AutoMirrored.Filled.ShowChart,
                        onClick = {
                            com.example.util.AppSoundManager.playTechClick()
                            onSendMessage(strings.aiPromptPrefixCycle)
                        }
                    )
                    QuickActionChip(
                        label = strings.aiQuickActionFutures,
                        icon = Icons.Default.ElectricBolt,
                        onClick = {
                            com.example.util.AppSoundManager.playTechClick()
                            onSendMessage(strings.aiPromptPrefixFutures)
                        }
                    )
                    QuickActionChip(
                        label = strings.aiQuickActionWhales,
                        icon = Icons.Default.Radar,
                        onClick = {
                            com.example.util.AppSoundManager.playTechClick()
                            onSendMessage(strings.aiPromptPrefixWhales)
                        }
                    )
                    QuickActionChip(
                        label = strings.aiQuickActionAltseason,
                        icon = Icons.Default.Hub,
                        onClick = {
                            com.example.util.AppSoundManager.playTechClick()
                            onSendMessage(strings.aiPromptPrefixAltseason)
                        }
                    )
                }

                // Input Bar & Send or Limit Banner
                if (isAiLimitReached) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(palette.surfaceElevated)
                            .border(1.dp, CopperAccent.copy(alpha = 0.5f))
                            .padding(14.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Daily Free Limit Reached (3/3). Upgrade to Pro for unlimited real-time AI intelligence.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CopperAccent,
                                textAlign = TextAlign.Center
                            )
                            androidx.compose.material3.Button(
                                onClick = onOpenProModal,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = CopperAccent),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = "Unlock Pro", fontWeight = FontWeight.Bold, color = Color(0xFF05050F))
                            }
                        }
                    }
                } else {
                    val canSend = inputText.isNotBlank() && !isLoading
                    val sendButtonBg: Color = if (canSend) CopperAccent else palette.border
                    val sendButtonIconTint: Color = if (canSend) Color(0xFF05050F) else palette.textMuted

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(palette.surfaceElevated)
                            .border(1.dp, palette.border)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isProUnlocked) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CopperAccent.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$aiQueriesRemaining/3 Left",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CopperAccent
                                )
                            }
                        }

                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("ai_input_field"),
                            placeholder = {
                                Text(
                                    text = strings.aiPromptPlaceholder,
                                    fontSize = 13.sp,
                                    color = palette.textMuted
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = palette.background,
                                unfocusedContainerColor = palette.background,
                                disabledContainerColor = palette.background,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = palette.textPrimary,
                                unfocusedTextColor = palette.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = false,
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (canSend) {
                                        val textToSend = inputText.trim()
                                        inputText = ""
                                        com.example.util.AppSoundManager.playTechClick()
                                        onSendMessage(textToSend)
                                    }
                                }
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(sendButtonBg)
                                .clickable(enabled = canSend) {
                                    val textToSend = inputText.trim()
                                    inputText = ""
                                    com.example.util.AppSoundManager.playTechClick()
                                    onSendMessage(textToSend)
                                }
                                .testTag("ai_send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = strings.aiAskButton,
                                tint = sendButtonIconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Disclaimer Footer
                Text(
                    text = strings.aiDisclaimer,
                    fontSize = 9.5.sp,
                    color = palette.textMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.background)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ChatMessageItem(message: AiChatMessage) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    val isUser = message.sender == AiMessageSender.USER
    val context = androidx.compose.ui.platform.LocalContext.current
    val greek = strings.language == com.example.data.model.AppLanguage.GREEK
    var reported by remember(message.id) { mutableStateOf(false) }
    val canReport = !isUser && !message.isThinking && !message.isError

    val bubbleBgColor: Color = if (isUser) CopperAccent.copy(alpha = 0.18f) else palette.surfaceElevated
    val bubbleBorderColor: Color = if (isUser) {
        CopperAccent.copy(alpha = 0.45f)
    } else if (message.isError) {
        MarketRed.copy(alpha = 0.5f)
    } else {
        palette.border
    }
    val bubbleShape = RoundedCornerShape(
        topStart = 14.dp,
        topEnd = 14.dp,
        bottomStart = if (isUser) 14.dp else 2.dp,
        bottomEnd = if (isUser) 2.dp else 14.dp
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, end = 8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CopperAccent.copy(alpha = 0.2f))
                    .border(1.dp, CopperAccent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CopperAccent,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.92f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(bubbleShape)
                    .background(bubbleBgColor)
                    .border(1.dp, bubbleBorderColor, bubbleShape)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (message.isThinking) {
                    ThinkingIndicator(strings.aiThinking)
                } else if (reported) {
                    Text(
                        text = if (greek) "Η απάντηση αναφέρθηκε και κρύφτηκε. Ευχαριστούμε." else "Answer reported and hidden. Thank you.",
                        fontSize = 12.sp,
                        color = palette.textMuted
                    )
                } else {
                    val cleanedText = message.text
                        .replace(Regex("^#{1,6}\\s*", setOf(RegexOption.MULTILINE)), "")
                        .replace(Regex("\\*\\*"), "")
                    Text(
                        text = cleanedText,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = if (message.isError) MarketRed else palette.textPrimary,
                        fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
            if (canReport && !reported) {
                Text(
                    text = if (greek) "Αναφορά απάντησης" else "Report answer",
                    fontSize = 11.sp,
                    color = palette.textMuted,
                    modifier = Modifier
                        .padding(top = 4.dp, start = 4.dp)
                        .clickable {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:")
                                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(com.example.util.AiReport.SUPPORT_EMAIL))
                                putExtra(android.content.Intent.EXTRA_SUBJECT, com.example.util.AiReport.subject(greek))
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    com.example.util.AiReport.body(message.id, message.timestamp, message.text, greek)
                                )
                            }
                            reported = true
                            try {
                                context.startActivity(intent)
                            } catch (_: android.content.ActivityNotFoundException) {
                                android.widget.Toast.makeText(
                                    context,
                                    com.example.util.AiReport.SUPPORT_EMAIL,
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun ThinkingIndicator(label: String) {
    val transition = rememberInfiniteTransition(label = "thinking_anim")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.alpha(alpha)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(CopperAccent)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = CopperAccent,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val palette = LocalAppColors.current

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(palette.surface)
            .border(1.dp, palette.border, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CopperAccent,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.textSecondary
        )
    }
}

@Composable
fun AiMarketFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current

    // Compact, static round button: it floats over every list, so it stays small and quiet.
    Surface(
        onClick = {
            com.example.util.AppSoundManager.playTechClick()
            onClick()
        },
        modifier = modifier
            .size(52.dp)
            .shadow(8.dp, CircleShape, ambientColor = CopperAccent, spotColor = CopperAccent)
            .semantics { contentDescription = strings.aiAssistantBadge },
        shape = CircleShape,
        color = palette.surfaceElevated,
        border = BorderStroke(1.5.dp, CopperAccent.copy(alpha = 0.8f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = CopperAccent,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

