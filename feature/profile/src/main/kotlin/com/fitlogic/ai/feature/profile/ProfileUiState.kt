package com.fitlogic.ai.feature.profile

import com.fitlogic.ai.core.domain.model.LanguagePreference
import com.fitlogic.ai.core.domain.model.ThemePreference
import com.fitlogic.ai.core.domain.model.UserProfile
import com.fitlogic.ai.core.domain.model.WeightUnit

data class ProfileUiState(
    val profile: UserProfile? = null,
    val displayNameInput: String = "",
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val languagePreference: LanguagePreference = LanguagePreference.TR,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val isLoading: Boolean = true,
    val message: String? = null,
    val isDeleteDialogVisible: Boolean = false,
    val deleteConfirmInput: String = "",
    val isDeleteInProgress: Boolean = false,
)
