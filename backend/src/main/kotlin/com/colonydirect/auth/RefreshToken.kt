package com.colonydirect.auth

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Persisted refresh tokens are stored as a SHA-256 hash, never the raw token
 * (mirrors password-hash practice; a DB leak alone can't be replayed as a live token).
 */
@Entity
@Table(name = "refresh_token")
class RefreshToken(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID = UUID.randomUUID(),

    @Column(name = "token_hash", nullable = false, unique = true)
    var tokenHash: String = "",

    @Column(name = "device_label")
    var deviceLabel: String? = null,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant = Instant.now(),

    @Column(name = "revoked_at")
    var revokedAt: Instant? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    fun isActive(now: Instant = Instant.now()): Boolean =
        revokedAt == null && expiresAt.isAfter(now)
}
