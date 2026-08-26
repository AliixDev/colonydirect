package com.colonydirect.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class RegisterRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank @field:Size(min = 8, max = 128) val password: String,
    @field:NotBlank val fullName: String,
    val phone: String? = null,
    val preferredLanguage: String = "en"
)

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class GoogleAuthRequest(
    @field:NotBlank val idToken: String
)

data class RefreshRequest(
    @field:NotBlank val refreshToken: String
)

data class LogoutRequest(
    @field:NotBlank val refreshToken: String
)

data class PasswordResetRequestDto(
    @field:Email @field:NotBlank val email: String
)

data class PasswordResetConfirmDto(
    @field:NotBlank val token: String,
    @field:NotBlank @field:Size(min = 8, max = 128) val newPassword: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
    val user: UserSummary
)

data class UserSummary(
    val id: UUID,
    val email: String?,
    val phone: String?,
    val fullName: String,
    val role: UserRole,
    val preferredLanguage: String
) {
    companion object {
        fun from(user: User) = UserSummary(
            id = user.id,
            email = user.email,
            phone = user.phone,
            fullName = user.fullName,
            role = user.role,
            preferredLanguage = user.preferredLanguage
        )
    }
}
