package com.colonydirect.app.network

import com.colonydirect.app.data.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that attaches the stored JWT access token as a
 * Bearer header on every outgoing request (except the auth endpoints
 * themselves, which don't need it).
 */
class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {

    // Paths that must NOT carry a token (they ARE the token-issuing calls)
    private val noAuthPaths = setOf(
        "/auth/register",
        "/auth/login",
        "/auth/refresh",
        "/auth/google"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        if (noAuthPaths.any { path.endsWith(it) }) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { tokenStore.getAccessToken() }

        val request = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
