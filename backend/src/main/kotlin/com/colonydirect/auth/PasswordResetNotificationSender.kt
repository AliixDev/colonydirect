package com.colonydirect.auth

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Abstraction over however password-reset emails actually get delivered.
 * Real SMTP/transactional-email provider wiring is a Notification module
 * concern (mirrors the PaymentGateway pattern from the Phase 10 slice --
 * abstraction now, real adapter once provider credentials exist).
 */
interface PasswordResetNotificationSender {
    fun sendPasswordResetEmail(user: User, rawToken: String)
}

@Component
class LoggingPasswordResetNotificationSender : PasswordResetNotificationSender {
    private val log = LoggerFactory.getLogger(LoggingPasswordResetNotificationSender::class.java)

    override fun sendPasswordResetEmail(user: User, rawToken: String) {
        // Dev/test stand-in: logs instead of sending. Never logs the raw token
        // in a real environment; swap for a real provider before production.
        log.info("Password reset requested for user {} (email delivery not yet wired)", user.id)
    }
}
