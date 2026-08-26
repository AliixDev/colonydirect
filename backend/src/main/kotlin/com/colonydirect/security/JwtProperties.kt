package com.colonydirect.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenTtlMinutes: Long = 15,
    val refreshTokenTtlDays: Long = 30
) {
    init {
        require(secret.length >= 32) {
            "app.jwt.secret must be at least 32 characters (set JWT_SECRET env var in production)"
        }
    }
}
