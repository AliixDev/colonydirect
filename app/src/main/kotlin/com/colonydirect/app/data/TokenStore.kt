package com.colonydirect.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.colonydirect.app.network.dto.UserSummary
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "colony_direct_auth")

class TokenStore(private val context: Context, private val gson: Gson) {

    private val store = context.dataStore

    companion object {
        private val KEY_ACCESS_TOKEN  = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_JSON     = stringPreferencesKey("user_json")
    }

    // ── Writes ────────────────────────────────────────────────────────────────

    suspend fun saveTokens(accessToken: String, refreshToken: String, user: UserSummary) {
        store.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_JSON]     = gson.toJson(user)
        }
    }

    suspend fun clearAll() {
        store.edit { it.clear() }
    }

    // ── Reads ─────────────────────────────────────────────────────────────────

    suspend fun getAccessToken(): String? =
        store.data.map { it[KEY_ACCESS_TOKEN] }.first()

    suspend fun getRefreshToken(): String? =
        store.data.map { it[KEY_REFRESH_TOKEN] }.first()

    /** Synchronous read used from OkHttp interceptor (called on IO thread). */
    fun getAccessTokenBlocking(): String? = runBlocking { getAccessToken() }

    // ── Flows ─────────────────────────────────────────────────────────────────

    val isLoggedInFlow: Flow<Boolean> =
        store.data.map { prefs -> prefs[KEY_ACCESS_TOKEN] != null }

    val getCurrentUserFlow: Flow<UserSummary?> =
        store.data.map { prefs ->
            prefs[KEY_USER_JSON]?.let { json ->
                try { gson.fromJson(json, UserSummary::class.java) } catch (_: Exception) { null }
            }
        }

    /** Best-effort synchronous name read for UI greeting — not critical path. */
    fun cachedUserName(): String = runBlocking {
        store.data.map { prefs ->
            prefs[KEY_USER_JSON]?.let { json ->
                try { gson.fromJson(json, UserSummary::class.java)?.fullName } catch (_: Exception) { null }
            }
        }.first() ?: ""
    }
}
