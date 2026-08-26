package com.colonydirect.notification

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class RegisterDeviceTokenRequest(
    @field:NotBlank val token: String,
    val deviceType: String = "ANDROID"
)

data class SendNotificationRequest(
    @field:NotNull val userId: UUID,
    @field:NotBlank val title: String,
    @field:NotBlank val body: String,
    val type: NotificationType = NotificationType.SYSTEM,
    val referenceId: UUID? = null
)

data class NotificationResponse(
    val id: UUID,
    val userId: UUID,
    val title: String,
    val body: String,
    val type: NotificationType,
    val referenceId: UUID?,
    val isRead: Boolean,
    val createdAt: Instant
)

data class UnreadCountResponse(
    val unreadCount: Long
)