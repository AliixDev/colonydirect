package com.colonydirect.auth

import com.colonydirect.security.GoogleTokenVerifier
import com.colonydirect.security.JwtService
import com.colonydirect.security.TokenHasher
import com.colonydirect.web.ApiException
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.Optional
import java.util.UUID

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var passwordResetTokenRepository: PasswordResetTokenRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtService: JwtService
    private lateinit var googleTokenVerifier: GoogleTokenVerifier
    private lateinit var notificationSender: PasswordResetNotificationSender
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        refreshTokenRepository = mockk()
        passwordResetTokenRepository = mockk()
        passwordEncoder = mockk()
        jwtService = mockk()
        googleTokenVerifier = mockk()
        notificationSender = mockk(relaxed = true)
        authService = AuthService(
            userRepository, refreshTokenRepository, passwordResetTokenRepository,
            passwordEncoder, jwtService, googleTokenVerifier, notificationSender
        )
        every { jwtService.generateAccessToken(any()) } returns "access-token"
        every { jwtService.accessTokenTtlSeconds() } returns 900L
        every { refreshTokenRepository.save(any()) } answers { firstArg() }
    }

    @Test
    fun `register rejects duplicate email`() {
        val request = RegisterRequest(email = "a@b.com", password = "password123", fullName = "A B")
        every { userRepository.existsByEmailAndDeletedAtIsNull("a@b.com") } returns true

        val ex = assertThrows(ApiException::class.java) { authService.register(request) }
        assertEquals("EMAIL_ALREADY_REGISTERED", ex.code)
    }

    @Test
    fun `register succeeds and issues token pair`() {
        val request = RegisterRequest(email = "new@b.com", password = "password123", fullName = "New User")
        every { userRepository.existsByEmailAndDeletedAtIsNull("new@b.com") } returns false
        every { passwordEncoder.encode("password123") } returns "hashed"
        every { userRepository.save(any()) } answers { firstArg() }

        val response = authService.register(request)

        assertEquals("access-token", response.accessToken)
        assertEquals(900L, response.expiresInSeconds)
        assertEquals("new@b.com", response.user.email)
        verify { userRepository.save(match { it.passwordHash == "hashed" && it.role == UserRole.CUSTOMER }) }
    }

    @Test
    fun `login fails with wrong password`() {
        val user = User(email = "a@b.com", passwordHash = "hashed", fullName = "A B")
        every { userRepository.findByEmailAndDeletedAtIsNull("a@b.com") } returns Optional.of(user)
        every { passwordEncoder.matches("wrong", "hashed") } returns false

        val ex = assertThrows(ApiException::class.java) {
            authService.login(LoginRequest("a@b.com", "wrong"), null)
        }
        assertEquals("INVALID_CREDENTIALS", ex.code)
    }

    @Test
    fun `login fails for deactivated account`() {
        val user = User(email = "a@b.com", passwordHash = "hashed", fullName = "A B", isActive = false)
        every { userRepository.findByEmailAndDeletedAtIsNull("a@b.com") } returns Optional.of(user)
        every { passwordEncoder.matches("correct", "hashed") } returns true

        val ex = assertThrows(ApiException::class.java) {
            authService.login(LoginRequest("a@b.com", "correct"), null)
        }
        assertEquals("ACCOUNT_INACTIVE", ex.code)
    }

    @Test
    fun `login succeeds and issues token pair`() {
        val user = User(email = "a@b.com", passwordHash = "hashed", fullName = "A B")
        every { userRepository.findByEmailAndDeletedAtIsNull("a@b.com") } returns Optional.of(user)
        every { passwordEncoder.matches("correct", "hashed") } returns true

        val response = authService.login(LoginRequest("a@b.com", "correct"), "test-device")

        assertEquals("access-token", response.accessToken)
        assertNotNull(response.refreshToken)
    }

    @Test
    fun `refresh rejects unknown token`() {
        every { refreshTokenRepository.findByTokenHash(any()) } returns Optional.empty()

        val ex = assertThrows(ApiException::class.java) {
            authService.refresh(RefreshRequest("bogus-token"))
        }
        assertEquals("INVALID_REFRESH_TOKEN", ex.code)
    }

    @Test
    fun `refresh rejects expired token`() {
        val userId = UUID.randomUUID()
        val expired = RefreshToken(
            userId = userId,
            tokenHash = TokenHasher.sha256("raw-token"),
            expiresAt = Instant.now().minusSeconds(3600)
        )
        every { refreshTokenRepository.findByTokenHash(any()) } returns Optional.of(expired)

        val ex = assertThrows(ApiException::class.java) {
            authService.refresh(RefreshRequest("raw-token"))
        }
        assertEquals("REFRESH_TOKEN_EXPIRED", ex.code)
    }

    @Test
    fun `refresh rotates token and issues new pair`() {
        val userId = UUID.randomUUID()
        val user = User(id = userId, email = "a@b.com", fullName = "A B")
        val stored = RefreshToken(
            userId = userId,
            tokenHash = TokenHasher.sha256("raw-token"),
            expiresAt = Instant.now().plusSeconds(3600)
        )
        every { refreshTokenRepository.findByTokenHash(any()) } returns Optional.of(stored)
        every { userRepository.findById(userId) } returns Optional.of(user)

        val response = authService.refresh(RefreshRequest("raw-token"))

        assertNotNull(stored.revokedAt)
        assertNotEquals("raw-token", response.refreshToken)
    }

    @Test
    fun `password reset does not reveal whether email exists`() {
        every { userRepository.findByEmailAndDeletedAtIsNull("ghost@b.com") } returns Optional.empty()

        assertDoesNotThrow {
            authService.requestPasswordReset(PasswordResetRequestDto("ghost@b.com"))
        }
        verify(exactly = 0) { passwordResetTokenRepository.save(any()) }
    }

    @Test
    fun `password reset confirm rejects expired token`() {
        val expired = PasswordResetToken(
            userId = UUID.randomUUID(),
            tokenHash = TokenHasher.sha256("reset-token"),
            expiresAt = Instant.now().minusSeconds(60)
        )
        every { passwordResetTokenRepository.findByTokenHash(any()) } returns Optional.of(expired)

        val ex = assertThrows(ApiException::class.java) {
            authService.confirmPasswordReset(PasswordResetConfirmDto("reset-token", "newPassword123"))
        }
        assertEquals("RESET_TOKEN_EXPIRED", ex.code)
    }

    @Test
    fun `password reset confirm updates password and revokes all sessions`() {
        val userId = UUID.randomUUID()
        val user = User(id = userId, email = "a@b.com", passwordHash = "old-hash", fullName = "A B")
        val resetToken = PasswordResetToken(
            userId = userId,
            tokenHash = TokenHasher.sha256("reset-token"),
            expiresAt = Instant.now().plusSeconds(3600)
        )
        every { passwordResetTokenRepository.findByTokenHash(any()) } returns Optional.of(resetToken)
        every { userRepository.findById(userId) } returns Optional.of(user)
        every { passwordEncoder.encode("newPassword123") } returns "new-hash"
        every { userRepository.save(any()) } answers { firstArg() }
        every { passwordResetTokenRepository.save(any()) } answers { firstArg() }
        every { refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(userId) } returns emptyList()

        authService.confirmPasswordReset(PasswordResetConfirmDto("reset-token", "newPassword123"))

        assertEquals("new-hash", user.passwordHash)
        assertNotNull(resetToken.consumedAt)
        verify { refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(userId) }
    }

    @Test
    fun `google login links existing email account`() {
        val user = User(email = "linked@b.com", fullName = "Linked User")
        every { googleTokenVerifier.verify("valid-id-token") } returns
            com.colonydirect.security.GoogleIdentity("google-sub-1", "linked@b.com", true, "Linked User")
        every { userRepository.findByGoogleSubAndDeletedAtIsNull("google-sub-1") } returns Optional.empty()
        every { userRepository.findByEmailAndDeletedAtIsNull("linked@b.com") } returns Optional.of(user)
        every { userRepository.save(any()) } answers { firstArg() }

        val response = authService.loginWithGoogle(GoogleAuthRequest("valid-id-token"), null)

        assertEquals("google-sub-1", user.googleSub)
        assertEquals("linked@b.com", response.user.email)
    }

    @Test
    fun `google login rejects unverifiable token`() {
        every { googleTokenVerifier.verify("bad-token") } returns null

        val ex = assertThrows(ApiException::class.java) {
            authService.loginWithGoogle(GoogleAuthRequest("bad-token"), null)
        }
        assertEquals("INVALID_GOOGLE_TOKEN", ex.code)
    }
}
