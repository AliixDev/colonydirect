package com.colonydirect.auth

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request))

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest, httpRequest: HttpServletRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.login(request, deviceLabel(httpRequest)))

    @PostMapping("/google")
    fun google(@Valid @RequestBody request: GoogleAuthRequest, httpRequest: HttpServletRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.loginWithGoogle(request, deviceLabel(httpRequest)))

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.refresh(request))

    @PostMapping("/logout")
    fun logout(@Valid @RequestBody request: LogoutRequest): ResponseEntity<Void> {
        authService.logout(request.refreshToken)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/logout-all")
    fun logoutAll(authentication: Authentication): ResponseEntity<Void> {
        authService.logoutAll(authentication.principal as UUID)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/password-reset/request")
    fun requestPasswordReset(@Valid @RequestBody request: PasswordResetRequestDto): ResponseEntity<Void> {
        authService.requestPasswordReset(request)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/password-reset/confirm")
    fun confirmPasswordReset(@Valid @RequestBody request: PasswordResetConfirmDto): ResponseEntity<Void> {
        authService.confirmPasswordReset(request)
        return ResponseEntity.noContent().build()
    }

    private fun deviceLabel(request: HttpServletRequest): String? =
        request.getHeader("User-Agent")?.take(255)
}
