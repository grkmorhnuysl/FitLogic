@file:Suppress("FunctionName", "UnusedPrivateMember")

package com.fitlogic.ai.feature.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlogic.ai.core.designsystem.theme.FitLogicTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    googleWebClientId: String,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onAuthenticated()
        }
    }
    AuthContent(
        state = state,
        modifier = modifier,
        isGoogleEnabled = googleWebClientId.isNotBlank(),
        onModeChange = viewModel::onModeChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        onContinueAsGuest = viewModel::continueAsGuest,
        onGoogleSignInClick = {
            val activity = context.findActivity()
            if (activity == null) {
                viewModel.onGoogleSignInError("Google girisi bu ekranda baslatilamadi.")
                return@AuthContent
            }
            scope.launch {
                val idTokenResult = requestGoogleIdToken(activity = activity, webClientId = googleWebClientId)
                if (idTokenResult.isSuccess) {
                    viewModel.signInWithGoogle(idTokenResult.getOrThrow())
                } else {
                    viewModel.onGoogleSignInError(
                        idTokenResult.exceptionOrNull()?.message ?: "Google girisi basarisiz.",
                    )
                }
            }
        },
    )
}

@Composable
private fun AuthContent(
    state: AuthUiState,
    isGoogleEnabled: Boolean,
    onModeChange: (AuthMode) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Hesap", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        RowModes(currentMode = state.mode, onModeChange = onModeChange)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth().testTag("auth_email"),
            label = { Text("Email") },
            singleLine = true,
        )
        if (state.mode != AuthMode.RESET_PASSWORD) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth().testTag("auth_password"),
                label = { Text("Sifre") },
                singleLine = true,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().testTag("auth_submit"),
            enabled = !state.isLoading,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            } else {
                Text(
                    text =
                        when (state.mode) {
                            AuthMode.LOGIN -> "Giris Yap"
                            AuthMode.REGISTER -> "Kayit Ol"
                            AuthMode.RESET_PASSWORD -> "Sifre Sifirla"
                        },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onGoogleSignInClick,
            modifier = Modifier.fillMaxWidth().testTag("auth_google"),
            enabled = isGoogleEnabled && !state.isLoading && !state.isGoogleLoading,
        ) {
            if (state.isGoogleLoading) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            } else {
                Text("Google ile devam et")
            }
        }

        if (!isGoogleEnabled) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Google girisi icin GOOGLE_WEB_CLIENT_ID tanimlanmamis.",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onContinueAsGuest,
            modifier = Modifier.fillMaxWidth().testTag("auth_guest"),
            enabled = !state.isLoading && !state.isGoogleLoading,
        ) {
            Text("Misafir olarak devam et")
        }

        if (!state.message.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state.message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("auth_message"))
        }
    }
}

@Composable
private fun RowModes(
    currentMode: AuthMode,
    onModeChange: (AuthMode) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = { onModeChange(AuthMode.LOGIN) }, enabled = currentMode != AuthMode.LOGIN) {
            Text("Giris")
        }
        TextButton(onClick = { onModeChange(AuthMode.REGISTER) }, enabled = currentMode != AuthMode.REGISTER) {
            Text("Kayit")
        }
        TextButton(
            onClick = { onModeChange(AuthMode.RESET_PASSWORD) },
            enabled = currentMode != AuthMode.RESET_PASSWORD,
        ) {
            Text("Sifre")
        }
    }
}

private suspend fun requestGoogleIdToken(
    activity: Activity,
    webClientId: String,
): Result<String> =
    runCatching {
        val option =
            GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()
        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()
        val result = CredentialManager.create(activity).getCredential(activity, request)
        val credential = result.credential
        val customCredential = credential as? CustomCredential ?: error("Google kimlik verisi alinamadi.")
        if (customCredential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            error("Google token tipi gecersiz.")
        }
        val googleCredential = GoogleIdTokenCredential.createFrom(customCredential.data)
        googleCredential.idToken
    }

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    FitLogicTheme {
        AuthContent(
            state = AuthUiState(),
            isGoogleEnabled = true,
            onModeChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onContinueAsGuest = {},
            onGoogleSignInClick = {},
        )
    }
}
