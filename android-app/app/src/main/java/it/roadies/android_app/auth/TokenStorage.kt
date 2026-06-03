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
    suspend fun saveAuthState(authStateJson: String) {
        context.tokenDataStore.edit { prefs ->
            prefs.clear()
            prefs[AUTH_STATE] = cryptoStore.encrypt(authStateJson)
        }
    }

    suspend fun getAuthStateJson(): String? {
        val encrypted = context.tokenDataStore.data.first()[AUTH_STATE]
        return encrypted?.let { cryptoStore.decrypt(it) }
    }

    suspend fun clearAuthState() {
        context.tokenDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        private val AUTH_STATE = stringPreferencesKey("auth_state")
    }
}
