package com.colonydirect.auth

import com.colonydirect.security.GoogleTokenVerifier
import com.colonydirect.security.JwtService
import com.colonydirect.security.TokenHasher
import com.colonydirect.web.ApiException
import com.colonydirect.web.ErrorCodes
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val googleTokenVerifier: GoogleTokenVerifier,
    private val notificationSender: PasswordResetNotificationSender
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email)) {
            throw ApiException(ErrorCodes.EMAIL_ALREADY_REGISTERED, "An account with this email already exists.", HttpStatus.CONFLICT.value())
        }
        request.phone?.let {
            if (userRepository.findByPhoneAndDeletedAtIsNull(it).isPresent) {
                throw ApiException(ErrorCodes.PHONE_ALREADY_REGISTERED, "An account with this phone number already exists.", HttpStatus.CONFLICT.value())
            }
        }

        val user = User(
            email = request.email,
            phone = request.phone,
            passwordHash = passwordEncoder.encode(request.password),
            fullName = request.fullName,
            preferredLanguage = request.preferredLanguage,
            role = UserRole.CUSTOMER
        )
        userRepository.save(user)
        return issueTokenPair(user, deviceLabel = null)
    }

    @Transactional
    fun login(request: LoginRequest, deviceLabel: String?): AuthResponse {
        val user = userRepository.findByEmailAndDeletedAtIsNull(request.email)
            .orElseThrow { ApiException(ErrorCodes.INVALID_CREDENTIALS, "Invalid email or password.", HttpStatus.UNAUTHORIZED.value()) }

        if (user.passwordHash == null || !passwordEncoder.matches(request.password, user.passwordHash)) {
            throw ApiException(ErrorCodes.INVALID_CREDENTIALS, "Invalid email or password.", HttpStatus.UNAUTHORIZED.value())
        }
        if (!user.isActive) {
            throw ApiException(ErrorCodes.ACCOUNT_INACTIVE, "This account has been deactivated.", HttpStatus.FORBIDDEN.value())
        }
        return issueTokenPair(user, deviceLabel)
    }

    @Transactional
    fun loginWithGoogle(request: GoogleAuthRequest, deviceLabel: String?): AuthResponse {
        val identity = googleTokenVerifier.verify(request.idToken)
            ?: throw ApiException(ErrorCodes.INVALID_GOOGLE_TOKEN, "Google sign-in token could not be verified.", HttpStatus.UNAUTHORIZED.value())

        val existing = userRepository.findByGoogleSubAndDeletedAtIsNull(identity.sub)
        val user = if (existing.isPresent) {
            existing.get()
        } else {
            val byEmail = identity.email?.let { userRepository.findByEmailAndDeletedAtIsNull(it) }
            if (byEmail != null && byEmail.isPresent) {
                // Link Google identity to an existing email/password account.
                val toLink = byEmail.get()
                toLink.googleSub = identity.sub
                userRepository.save(toLink)
            } else {
                val newUser = User(
                    email = identity.email,
                    googleSub = identity.sub,
                    fullName = identity.name ?: identity.email ?: "Colony Direct User",
                    role = UserRole.CUSTOMER
                )
                userRepository.save(newUser)
            }
        }
        if (!user.isActive) {
            throw ApiException(ErrorCodes.ACCOUNT_INACTIVE, "This account has been deactivated.", HttpStatus.FORBIDDEN.value())
        }
        return issueTokenPair(user, deviceLabel)
    }

    @Transactional
    fun refresh(request: RefreshRequest): AuthResponse {
        val tokenHash = TokenHasher.sha256(request.refreshToken)
        val stored = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow { ApiException(ErrorCodes.INVALID_REFRESH_TOKEN, "Refresh token is invalid.", HttpStatus.UNAUTHORIZED.value()) }

        if (stored.revokedAt != null) {
            throw ApiException(ErrorCodes.INVALID_REFRESH_TOKEN, "Refresh token has been revoked.", HttpStatus.UNAUTHORIZED.value())
        }
        if (!stored.isActive()) {
            throw ApiException(ErrorCodes.REFRESH_TOKEN_EXPIRED, "Refresh token has expired, please log in again.", HttpStatus.UNAUTHORIZED.value())
        }

        val user = userRepository.findById(stored.userId)
            .orElseThrow { ApiException(ErrorCodes.USER_NOT_FOUND, "User not found.", HttpStatus.NOT_FOUND.value()) }

        // Rotate: revoke the used refresh token and issue a brand new pair.
        stored.revokedAt = Instant.now()
        refreshTokenRepository.save(stored)
        return issueTokenPair(user, stored.deviceLabel)
    }

    @Transactional
    fun logout(refreshToken: String) {
        val tokenHash = TokenHasher.sha256(refreshToken)
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent {
            it.revokedAt = Instant.now()
            refreshTokenRepository.save(it)
        }
    }

    @Transactional
    fun logoutAll(userId: UUID) {
        refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(userId).forEach {
            it.revokedAt = Instant.now()
            refreshTokenRepository.save(it)
        }
    }

    @Transactional
    fun requestPasswordReset(request: PasswordResetRequestDto) {
        val userOpt = userRepository.findByEmailAndDeletedAtIsNull(request.email)
        // Deliberately do not reveal whether the email exists (enumeration protection):
        // always return normally, only send the email if a matching account was found.
        if (userOpt.isEmpty) return

        val rawToken = TokenHasher.generateOpaqueToken()
        val resetToken = PasswordResetToken(
            userId = userOpt.get().id,
            tokenHash = TokenHasher.sha256(rawToken),
            expiresAt = Instant.now().plus(1, ChronoUnit.HOURS)
        )
        passwordResetTokenRepository.save(resetToken)
        notificationSender.sendPasswordResetEmail(userOpt.get(), rawToken)
    }

    @Transactional
    fun confirmPasswordReset(request: PasswordResetConfirmDto) {
        val tokenHash = TokenHasher.sha256(request.token)
        val stored = passwordResetTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow { ApiException(ErrorCodes.INVALID_RESET_TOKEN, "Reset token is invalid.", HttpStatus.UNPROCESSABLE_ENTITY.value()) }

        if (!stored.isUsable()) {
            throw ApiException(ErrorCodes.RESET_TOKEN_EXPIRED, "Reset token has expired or already been used.", HttpStatus.UNPROCESSABLE_ENTITY.value())
        }

        val user = userRepository.findById(stored.userId)
            .orElseThrow { ApiException(ErrorCodes.USER_NOT_FOUND, "User not found.", HttpStatus.NOT_FOUND.value()) }

        user.passwordHash = passwordEncoder.encode(request.newPassword)
        user.updatedAt = Instant.now()
        userRepository.save(user)

        stored.consumedAt = Instant.now()
        passwordResetTokenRepository.save(stored)

        // Defense in depth: a password reset invalidates all existing sessions.
        logoutAll(user.id)
    }

    private fun issueTokenPair(user: User, deviceLabel: String?): AuthResponse {
        val accessToken = jwtService.generateAccessToken(user)
        val rawRefreshToken = TokenHasher.generateOpaqueToken()
        val refreshTokenEntity = RefreshToken(
            userId = user.id,
            tokenHash = TokenHasher.sha256(rawRefreshToken),
            deviceLabel = deviceLabel,
            expiresAt = Instant.now().plus(30, ChronoUnit.DAYS)
        )
        refreshTokenRepository.save(refreshTokenEntity)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            expiresInSeconds = jwtService.accessTokenTtlSeconds(),
            user = UserSummary.from(user)
        )
    }
}
