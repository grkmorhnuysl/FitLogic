package com.fitlogic.ai.core.data.local.session

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.fitlogic.ai.core.data.remote.AuthProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class SessionPreferencesTest {
    @Test
    fun setAuthenticatedAndSignedOut_shouldPersistAndClearSessionTokens() =
        runTest {
            val dataStoreFile = File.createTempFile("session-test", ".preferences_pb")
            val dataStore = PreferenceDataStoreFactory.create(produceFile = { dataStoreFile })
            val sessionPreferences = SessionPreferences(dataStore)

            sessionPreferences.setAuthenticated(
                userId = "user-1",
                email = "user@example.com",
                accessToken = "access-1",
                refreshToken = "refresh-1",
                expiresAtEpochSeconds = 1234L,
                provider = AuthProvider.EMAIL,
            )
            val authenticated = sessionPreferences.sessionSnapshot.first()
            assertEquals("user-1", authenticated.currentUserId)
            assertEquals("access-1", authenticated.accessToken)
            assertEquals("refresh-1", authenticated.refreshToken)
            assertEquals(1234L, authenticated.expiresAtEpochSeconds)
            assertEquals(AuthProvider.EMAIL, authenticated.provider)

            sessionPreferences.setSignedOut()
            val signedOut = sessionPreferences.sessionSnapshot.first()
            assertNull(signedOut.currentUserId)
            assertNull(signedOut.accessToken)
            assertNull(signedOut.refreshToken)
            assertNull(signedOut.expiresAtEpochSeconds)
            assertNull(signedOut.provider)
        }
}
