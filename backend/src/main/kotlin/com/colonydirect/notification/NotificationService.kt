package com.colonydirect.notification

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class NotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val notificationRepository: NotificationRepository
) {

    fun registerDeviceToken(userId: UUID, request: RegisterDeviceTokenRequest) {
        val existingToken = deviceTokenRepository.findByToken(request.token)
        if (existingToken != null) {
            existingToken.token = request.token
            existingToken.deviceType = request.deviceType
            deviceTokenRepository.save(existingToken)
        } else {
            deviceTokenRepository.save(
                DeviceToken(
                    userId = userId,
                    token = request.token,
                    deviceType = request.deviceType
                )
            )
        }
    }

    fun sendNotification(request: SendNotificationRequest): NotificationResponse {
        val notification = notificationRepository.save(
            Notification(
                userId = request.userId,
                title = request.title,
                body = request.body,
                type = request.type,
                referenceId = request.referenceId
            )
        )
        return notification.toResponse()
    }

    @Transactional(readOnly = true)
    fun getUserNotifications(userId: UUID, pageable: Pageable): Page<NotificationResponse> {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map { it.toResponse() }
    }

    fun markAsRead(userId: UUID, notificationId: UUID): NotificationResponse {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { NoSuchElementException("Notification not found") }

        require(notification.userId == userId) { "Notification does not belong to user" }

        notification.isRead = true
        return notificationRepository.save(notification).toResponse()
    }

    @Transactional(readOnly = true)
    fun getUnreadCount(userId: UUID): UnreadCountResponse {
        val count = notificationRepository.countByUserIdAndIsReadFalse(userId)
        return UnreadCountResponse(unreadCount = count)
    }

    private fun Notification.toResponse() = NotificationResponse(
        id = id,
        userId = userId,
        title = title,
        body = body,
        type = type,
        referenceId = referenceId,
        isRead = isRead,
        createdAt = createdAt
    )
}