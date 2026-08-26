package com.colonydirect.app.network.dto

import com.google.gson.annotations.SerializedName

// ─── Request DTOs ────────────────────────────────────────────────────────────

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("preferredLanguage") val preferredLanguage: String = "en"
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RefreshRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class LogoutRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

// ─── Response DTOs ───────────────────────────────────────────────────────────

data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("expiresInSeconds") val expiresInSeconds: Long,
    @SerializedName("user") val user: UserSummary
)

data class UserSummary(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("role") val role: String,
    @SerializedName("preferredLanguage") val preferredLanguage: String
)

// ─── API Error ───────────────────────────────────────────────────────────────

data class ApiErrorResponse(
    @SerializedName("status") val status: Int?,
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("path") val path: String?
)
