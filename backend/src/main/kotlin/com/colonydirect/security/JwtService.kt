package com.colonydirect.security

import com.colonydirect.auth.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

data class AccessTokenClaims(val userId: UUID, val role: String)

/**
 * Issues and verifies short-lived JWT access tokens. Refresh tokens are opaque
 * random strings persisted (hashed) in `refresh_token` -- NOT JWTs -- so they
 * can be individually revoked (logout / logout-all), which a stateless JWT can't.
 */
@Component
class JwtService(private val props: JwtProperties) {

    private val signingKey: SecretKey = Keys.hmacShaKeyFor(props.secret.toByteArray())

    fun generateAccessToken(user: User): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(props.accessTokenTtlMinutes * 60)
        return Jwts.builder()
            .subject(user.id.toString())
            .claim("role", user.role.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(signingKey)
            .compact()
    }

    fun accessTokenTtlSeconds(): Long = props.accessTokenTtlMinutes * 60

    fun parseAccessToken(token: String): AccessTokenClaims? = try {
        val claims: Claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
        AccessTokenClaims(
            userId = UUID.fromString(claims.subject),
            role = claims["role"] as String
        )
    } catch (e: ExpiredJwtException) {
        null
    } catch (e: Exception) {
        null
    }
}
