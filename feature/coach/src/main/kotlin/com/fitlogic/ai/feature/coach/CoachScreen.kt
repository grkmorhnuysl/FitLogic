@file:Suppress("FunctionName")

package com.fitlogic.ai.feature.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlogic.ai.core.designsystem.component.FlCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CoachScreen(
    modifier: Modifier = Modifier,
    viewModel: CoachViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val chatListState = rememberLazyListState()

    LaunchedEffect(state.chatMessages.size, state.isSending) {
        if (state.chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(state.chatMessages.lastIndex)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shadowElevation = 3.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("AI", fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "FitLogic Koc",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.testTag("coach_title"),
                        )
                        Text("Telefonda aktif", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CoachActionButton(
                        onClick = viewModel::generateWeeklyReport,
                        modifier = Modifier.testTag("coach_generate_weekly"),
                    ) {
                        Text("Haftalik Rapor")
                    }
                    CoachActionButton(
                        onClick = viewModel::detectPlateau,
                        modifier = Modifier.testTag("coach_detect_plateau"),
                    ) {
                        Text("Plato Kontrol")
                    }
                }
            }
        }

        LazyColumn(
            state = chatListState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.chatMessages.isEmpty()) {
                item {
                    CoachBubble(
                        message =
                            CoachChatMessage(
                                role = CoachMessageRole.ASSISTANT,
                                text = "Bugun neye odaklanalim? Antrenman, beslenme veya motivasyon icin yazabilirsin.",
                            ),
                    )
                }
            }

            state.selectedInsight?.let { selected ->
                item {
                    FlCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("Insight Detay", style = MaterialTheme.typography.titleMedium)
                            Text(selected.title, style = MaterialTheme.typography.titleSmall)
                            Text(selected.body)
                        }
                    }
                }
            }

            items(state.chatMessages) { message ->
                CoachBubble(message = message)
            }

            if (state.isSending) {
                item {
                    CoachBubble(
                        message = CoachChatMessage(CoachMessageRole.ASSISTANT, "Yanit hazirlaniyor..."),
                    )
                }
            }

            if (state.insights.isNotEmpty()) {
                item {
                    Text("Son analizler", style = MaterialTheme.typography.titleMedium)
                }
                items(state.insights) { insight ->
                    FlCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(insight.title, style = MaterialTheme.typography.titleMedium)
                            Text(insight.body.take(140))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { viewModel.selectInsight(insight.id) }) {
                                    Text("Detay")
                                }
                                OutlinedButton(onClick = { viewModel.markAsRead(insight.id) }) {
                                    Text("Okundu")
                                }
                            }
                        }
                    }
                }
            }

            state.message?.let { message ->
                item {
                    Text(message, modifier = Modifier.testTag("coach_status_message"))
                }
            }
        }

        CoachInputBar(state = state, viewModel = viewModel)
    }
}

@Composable
private fun CoachActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 40.dp),
        shape = RoundedCornerShape(18.dp),
        content = content,
    )
}

@Composable
private fun CoachBubble(message: CoachChatMessage) {
    val isUser = message.role == CoachMessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Card(
            modifier = Modifier.widthIn(max = 330.dp),
            shape =
                RoundedCornerShape(
                    topStart = if (isUser) 16.dp else 4.dp,
                    topEnd = if (isUser) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp,
                ),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        if (isUser) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    contentColor =
                        if (isUser) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    message.text,
                    modifier = Modifier.testTag(if (isUser) "coach_chat_user_message" else "coach_chat_ai_message"),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    formatMessageTime(message.createdAt),
                    modifier = Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CoachInputBar(
    state: CoachUiState,
    viewModel: CoachViewModel,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            state.chatError?.let { errorText ->
                Text(errorText, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("coach_chat_error"))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconButton(
                    onClick = viewModel::retryLastMessage,
                    enabled = !state.isSending,
                    modifier = Modifier.testTag("coach_chat_retry"),
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Tekrar dene")
                }
                TextField(
                    value = state.draftMessage,
                    onValueChange = viewModel::onDraftChanged,
                    modifier = Modifier.weight(1f).testTag("coach_chat_input"),
                    placeholder = { Text("Mesaj") },
                    minLines = 1,
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
                        ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                )
                Button(
                    onClick = viewModel::sendMessage,
                    enabled = !state.isSending,
                    modifier = Modifier.size(48.dp).testTag("coach_chat_send"),
                    shape = CircleShape,
                    contentPadding = ButtonDefaults.ContentPadding,
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gonder")
                }
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
