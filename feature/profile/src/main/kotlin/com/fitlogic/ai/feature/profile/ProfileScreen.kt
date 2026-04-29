@file:Suppress("FunctionName", "UnusedPrivateMember")

package com.fitlogic.ai.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlogic.ai.core.designsystem.theme.FitLogicTheme
import com.fitlogic.ai.core.domain.model.LanguagePreference
import com.fitlogic.ai.core.domain.model.NotificationType
import com.fitlogic.ai.core.domain.model.ThemePreference
import com.fitlogic.ai.core.domain.model.WeightUnit

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    ProfileContent(
        state = state,
        modifier = modifier,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onThemeChange = viewModel::onThemeChange,
        onLanguageChange = viewModel::onLanguageChange,
        onWeightUnitChange = viewModel::onWeightUnitChange,
        onSaveProfile = viewModel::saveProfile,
        onSavePreferences = viewModel::savePreferencesOnly,
        onExportData = viewModel::exportData,
        onSignOut = viewModel::signOut,
        onRequestDeleteAccount = viewModel::requestDeleteAccount,
        onDeleteConfirmInputChange = viewModel::onDeleteConfirmInputChange,
        onCancelDelete = viewModel::cancelDeleteAccount,
        onConfirmDelete = viewModel::confirmDeleteAccount,
        onNotificationToggle = viewModel::setNotificationEnabled,
        onSyncNow = viewModel::syncNow,
    )
}

@Composable
@Suppress("LongParameterList")
private fun ProfileContent(
    state: ProfileUiState,
    onDisplayNameChange: (String) -> Unit,
    onThemeChange: (ThemePreference) -> Unit,
    onLanguageChange: (LanguagePreference) -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onSaveProfile: () -> Unit,
    onSavePreferences: () -> Unit,
    onExportData: () -> Unit,
    onSignOut: () -> Unit,
    onRequestDeleteAccount: () -> Unit,
    onDeleteConfirmInputChange: (String) -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onNotificationToggle: (NotificationType, Boolean) -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "Profil ve Ayarlar", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = state.displayNameInput,
            onValueChange = onDisplayNameChange,
            modifier = Modifier.fillMaxWidth().testTag("profile_display_name"),
            label = { Text("Ad Soyad") },
        )
        Text("Tema: ${state.themePreference.name}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onThemeChange(ThemePreference.SYSTEM) }) { Text("Sistem") }
            TextButton(onClick = { onThemeChange(ThemePreference.LIGHT) }) { Text("Acik") }
            TextButton(onClick = { onThemeChange(ThemePreference.DARK) }) { Text("Koyu") }
        }
        Text("Dil: ${state.languagePreference.name}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onLanguageChange(LanguagePreference.TR) }) { Text("TR") }
            TextButton(onClick = { onLanguageChange(LanguagePreference.EN) }) { Text("EN") }
        }
        Text("Agirlik Birimi: ${state.weightUnit.name}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onWeightUnitChange(WeightUnit.KG) }) { Text("KG") }
            TextButton(onClick = { onWeightUnitChange(WeightUnit.LB) }) { Text("LB") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSaveProfile, modifier = Modifier.testTag("profile_save")) { Text("Profili Kaydet") }
            Button(onClick = onSavePreferences) { Text("Ayarlari Kaydet") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onExportData) { Text("Veri Disa Aktar") }
            TextButton(onClick = onSignOut) { Text("Cikis Yap") }
            TextButton(onClick = onRequestDeleteAccount, modifier = Modifier.testTag("profile_delete_request")) {
                Text("Hesabi Sil")
            }
        }
        Text("Bildirim Ayarlari")
        NotificationToggleRow(
            label = "Antrenman hatirlaticisi",
            testTag = "profile_notification_workout",
            checked = state.notificationSettings.workoutReminderEnabled,
            onCheckedChange = { onNotificationToggle(NotificationType.WORKOUT_REMINDER, it) },
        )
        NotificationToggleRow(
            label = "Su hatirlaticisi",
            testTag = "profile_notification_water",
            checked = state.notificationSettings.waterReminderEnabled,
            onCheckedChange = { onNotificationToggle(NotificationType.WATER_REMINDER, it) },
        )
        NotificationToggleRow(
            label = "Haftalik rapor",
            testTag = "profile_notification_weekly",
            checked = state.notificationSettings.weeklyReportEnabled,
            onCheckedChange = { onNotificationToggle(NotificationType.WEEKLY_REPORT, it) },
        )
        NotificationToggleRow(
            label = "PR kutlamasi",
            testTag = "profile_notification_pr",
            checked = state.notificationSettings.prCelebrationEnabled,
            onCheckedChange = { onNotificationToggle(NotificationType.PR_CELEBRATION, it) },
        )
        NotificationToggleRow(
            label = "Streak koruma",
            testTag = "profile_notification_streak",
            checked = state.notificationSettings.streakSaveEnabled,
            onCheckedChange = { onNotificationToggle(NotificationType.STREAK_SAVE, it) },
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text("Senkronizasyon")
        Text("Bekleyen kayit: ${state.syncStatus.pendingCount}")
        Text("Son sync: ${state.syncStatus.lastSyncAtEpochMs ?: 0}")
        Text("Son pull: ${state.syncStatus.lastPullAtEpochMs ?: 0}")
        Button(onClick = onSyncNow, modifier = Modifier.testTag("profile_sync_now")) { Text("Simdi Senkronize Et") }
        if (!state.message.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = state.message, modifier = Modifier.testTag("profile_message"))
        }
    }

    if (state.isDeleteDialogVisible) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Hesabi Sil") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hesabi silmek icin asagidaki metni yazin:")
                    Text(ProfileViewModel.DELETE_CONFIRM_PHRASE)
                    OutlinedTextField(
                        value = state.deleteConfirmInput,
                        onValueChange = onDeleteConfirmInputChange,
                        modifier = Modifier.fillMaxWidth().testTag("profile_delete_confirm_input"),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    enabled = !state.isDeleteInProgress && state.deleteConfirmInput == ProfileViewModel.DELETE_CONFIRM_PHRASE,
                    modifier = Modifier.testTag("profile_delete_confirm"),
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancelDelete,
                    enabled = !state.isDeleteInProgress,
                    modifier = Modifier.testTag("profile_delete_cancel"),
                ) {
                    Text("Vazgec")
                }
            },
        )
    }
}

@Composable
private fun NotificationToggleRow(
    label: String,
    testTag: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.testTag(testTag))
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    FitLogicTheme {
        ProfileContent(
            state = ProfileUiState(isLoading = false),
            onDisplayNameChange = {},
            onThemeChange = {},
            onLanguageChange = {},
            onWeightUnitChange = {},
            onSaveProfile = {},
            onSavePreferences = {},
            onExportData = {},
            onSignOut = {},
            onRequestDeleteAccount = {},
            onDeleteConfirmInputChange = {},
            onCancelDelete = {},
            onConfirmDelete = {},
            onNotificationToggle = { _, _ -> },
            onSyncNow = {},
        )
    }
}
