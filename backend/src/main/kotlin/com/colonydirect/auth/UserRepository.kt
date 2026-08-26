package com.colonydirect.auth

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmailAndDeletedAtIsNull(email: String): Optional<User>
    fun findByPhoneAndDeletedAtIsNull(phone: String): Optional<User>
    fun findByGoogleSubAndDeletedAtIsNull(googleSub: String): Optional<User>
    fun existsByEmailAndDeletedAtIsNull(email: String): Boolean
}

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByTokenHash(tokenHash: String): Optional<RefreshToken>
    fun findAllByUserIdAndRevokedAtIsNull(userId: UUID): List<RefreshToken>
}
