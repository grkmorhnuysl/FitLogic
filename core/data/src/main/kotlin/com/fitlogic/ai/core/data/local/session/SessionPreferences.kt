package com.fitlogic.ai.core.data.local.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fitlogic.ai.core.data.remote.AuthProvider
import com.fitlogic.ai.core.domain.model.UserAuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class SessionSnapshot(
    val onboardingCompleted: Boolean = false,
    val authState: UserAuthState = UserAuthState.SignedOut,
    val currentUserId: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresAtEpochSeconds: Long? = null,
    val provider: AuthProvider? = null,
)

@Singleton
class SessionPreferences
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val sessionSnapshot: Flow<SessionSnapshot> =
            dataStore.data.map { preferences ->
                val authStateValue = preferences[KEY_AUTH_STATE] ?: AUTH_STATE_SIGNED_OUT
                val userId = preferences[KEY_CURRENT_USER_ID]
                val email = preferences[KEY_AUTH_EMAIL] ?: ""
                val providerValue = preferences[KEY_AUTH_PROVIDER]
                val provider = providerValue?.let { value -> AuthProvider.entries.firstOrNull { it.name == value } }
                val authState =
                    when (authStateValue) {
                        AUTH_STATE_AUTHENTICATED ->
                            if (userId.isNullOrBlank() || email.isBlank()) {
                                UserAuthState.SignedOut
                            } else {
                                UserAuthState.Authenticated(userId = userId, email = email)
                            }
                        AUTH_STATE_GUEST -> UserAuthState.Guest
                        else -> UserAuthState.SignedOut
                    }
                SessionSnapshot(
                    onboardingCompleted = preferences[KEY_ONBOARDING_COMPLETED] ?: false,
                    authState = authState,
                    currentUserId = userId,
                    accessToken = preferences[KEY_ACCESS_TOKEN],
                    refreshToken = preferences[KEY_REFRESH_TOKEN],
                    expiresAtEpochSeconds = preferences[KEY_EXPIRES_AT_EPOCH_SECONDS],
                    provider = provider,
                )
            }

        suspend fun markOnboardingCompleted(completed: Boolean) {
            dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
        }

        suspend fun setSignedOut() {
            dataStore.edit {
                it[KEY_AUTH_STATE] = AUTH_STATE_SIGNED_OUT
                it.remove(KEY_AUTH_EMAIL)
                it.remove(KEY_ACCESS_TOKEN)
                it.remove(KEY_REFRESH_TOKEN)
                it.remove(KEY_EXPIRES_AT_EPOCH_SECONDS)
                it.remove(KEY_AUTH_PROVIDER)
                it.remove(KEY_CURRENT_USER_ID)
            }
        }

        suspend fun setGuest() {
            dataStore.edit {
                it[KEY_AUTH_STATE] = AUTH_STATE_GUEST
                it.remove(KEY_AUTH_EMAIL)
                it.remove(KEY_ACCESS_TOKEN)
                it.remove(KEY_REFRESH_TOKEN)
                it.remove(KEY_EXPIRES_AT_EPOCH_SECONDS)
                it.remove(KEY_AUTH_PROVIDER)
            }
        }

        suspend fun setAuthenticated(
            userId: String,
            email: String,
            accessToken: String,
            refreshToken: String,
            expiresAtEpochSeconds: Long?,
            provider: AuthProvider,
        ) {
            dataStore.edit {
                it[KEY_CURRENT_USER_ID] = userId
                it[KEY_AUTH_EMAIL] = email
                it[KEY_AUTH_STATE] = AUTH_STATE_AUTHENTICATED
                it[KEY_ACCESS_TOKEN] = accessToken
                it[KEY_REFRESH_TOKEN] = refreshToken
                it[KEY_AUTH_PROVIDER] = provider.name
                if (expiresAtEpochSeconds != null) {
                    it[KEY_EXPIRES_AT_EPOCH_SECONDS] = expiresAtEpochSeconds
                } else {
                    it.remove(KEY_EXPIRES_AT_EPOCH_SECONDS)
                }
            }
        }

        suspend fun updateTokens(
            accessToken: String,
            refreshToken: String,
            expiresAtEpochSeconds: Long?,
        ) {
            dataStore.edit {
                it[KEY_ACCESS_TOKEN] = accessToken
                it[KEY_REFRESH_TOKEN] = refreshToken
                if (expiresAtEpochSeconds != null) {
                    it[KEY_EXPIRES_AT_EPOCH_SECONDS] = expiresAtEpochSeconds
                } else {
                    it.remove(KEY_EXPIRES_AT_EPOCH_SECONDS)
                }
            }
        }

        suspend fun setCurrentUserId(userId: String) {
            dataStore.edit { it[KEY_CURRENT_USER_ID] = userId }
        }

        suspend fun clearAll() {
            dataStore.edit { it.clear() }
        }

        companion object {
            private const val AUTH_STATE_SIGNED_OUT = "signed_out"
            private const val AUTH_STATE_AUTHENTICATED = "authenticated"
            private const val AUTH_STATE_GUEST = "guest"
            private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
            private val KEY_AUTH_STATE = stringPreferencesKey("auth_state")
            private val KEY_CURRENT_USER_ID = stringPreferencesKey("current_user_id")
            private val KEY_AUTH_EMAIL = stringPreferencesKey("auth_email")
            private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
            private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
            private val KEY_EXPIRES_AT_EPOCH_SECONDS = longPreferencesKey("expires_at_epoch_seconds")
            private val KEY_AUTH_PROVIDER = stringPreferencesKey("auth_provider")
        }
    }
