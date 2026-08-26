package com.colonydirect.app.data

import com.colonydirect.app.network.ApiClient
import com.colonydirect.app.network.AuthApi
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.dto.AuthResponse
import com.colonydirect.app.network.dto.LoginRequest
import com.colonydirect.app.network.dto.LogoutRequest
import com.colonydirect.app.network.dto.RefreshRequest
import com.colonydirect.app.network.dto.RegisterRequest
import com.colonydirect.app.network.dto.UserSummary
import com.colonydirect.app.network.safeApiCall
import com.colonydirect.app.network.safeApiCallNoBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

/**
 * Single source of truth for all authentication operations.
 */
class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) {

    // ─── Flows ────────────────────────────────────────────────────────────────

    /** Emits true when a valid access token is stored locally. */
    val isLoggedInFlow: Flow<Boolean> get() = tokenStore.isLoggedInFlow

    /**
     * Emits the cached user summary whenever it changes.
     * Named both ways for compatibility with callers:
     *  - AuthViewModel (Step 1) calls currentUserFlow
     *  - ProfileViewModel (Step 4) calls getCurrentUserFlow
     */
    val currentUserFlow: Flow<UserSummary?> get() = tokenStore.getCurrentUserFlow
    val getCurrentUserFlow: Flow<UserSummary?> get() = tokenStore.getCurrentUserFlow

    // ─── Registration ─────────────────────────────────────────────────────────

    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String? = null
    ): NetworkResult<AuthResponse> {
        val result = safeApiCall(ApiClient.gson) {
            authApi.register(
                RegisterRequest(email = email, password = password, fullName = fullName, phone = phone)
            )
        }
        if (result is NetworkResult.Success) {
            tokenStore.saveTokens(result.data.accessToken, result.data.refreshToken, result.data.user)
        }
        return result
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        val result = safeApiCall(ApiClient.gson) {
            authApi.login(LoginRequest(email = email, password = password))
        }
        if (result is NetworkResult.Success) {
            tokenStore.saveTokens(result.data.accessToken, result.data.refreshToken, result.data.user)
        }
        return result
    }

    // ─── Token Refresh ────────────────────────────────────────────────────────

    suspend fun refreshAccessToken(): Boolean {
        val refreshToken = tokenStore.getRefreshToken() ?: return false
        val result = safeApiCall(ApiClient.gson) {
            authApi.refresh(RefreshRequest(refreshToken = refreshToken))
        }
        return if (result is NetworkResult.Success) {
            tokenStore.saveTokens(result.data.accessToken, result.data.refreshToken, result.data.user)
            true
        } else false
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    suspend fun logout(): NetworkResult<Unit> {
        val refreshToken = tokenStore.getRefreshToken() ?: run {
            tokenStore.clearAll()
            return NetworkResult.Success(Unit)
        }
        safeApiCallNoBody { authApi.logout(LogoutRequest(refreshToken = refreshToken)) }
        // Always clear local tokens regardless of server response
        tokenStore.clearAll()
        return NetworkResult.Success(Unit)
    }

    // ─── Convenience ─────────────────────────────────────────────────────────

    val isLoggedIn: Boolean
        get() = runBlocking { tokenStore.getAccessToken() != null }
}
