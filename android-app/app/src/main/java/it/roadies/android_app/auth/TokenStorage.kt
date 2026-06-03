package it.roadies.android_app.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.tokenDataStore by preferencesDataStore(name = "token_storage")

class TokenStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val cryptoStore: CryptoStore
) {
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        context.tokenDataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = cryptoStore.encrypt(accessToken)
            prefs[REFRESH_TOKEN] = cryptoStore.encrypt(refreshToken)
        }
    }

    suspend fun getAccessToken(): String? {
        val encrypted = context.tokenDataStore.data.first()[ACCESS_TOKEN]
        return encrypted?.let { cryptoStore.decrypt(it) }
    }

    suspend fun getRefreshToken(): String? {
        val encrypted = context.tokenDataStore.data.first()[REFRESH_TOKEN]
        return encrypted?.let { cryptoStore.decrypt(it) }
    }

    suspend fun clearTokens() {
        context.tokenDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}
