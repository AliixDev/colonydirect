package com.colonydirect.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Opaque refresh/reset tokens are random bytes, base64url-encoded for transport,
 * and stored only as a SHA-256 hash (never the raw value) -- a DB read alone
 * can't be replayed as a live token.
 */
object TokenHasher {
    private val secureRandom = SecureRandom()
    private val encoder = Base64.getUrlEncoder().withoutPadding()

    fun generateOpaqueToken(byteLength: Int = 48): String {
        val bytes = ByteArray(byteLength)
        secureRandom.nextBytes(bytes)
        return encoder.encodeToString(bytes)
    }

    fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
