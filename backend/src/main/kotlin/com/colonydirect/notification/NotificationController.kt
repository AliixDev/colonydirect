package com.colonydirect.notification

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @PostMapping("/device-token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun registerDeviceToken(
        authentication: Authentication,
        @Valid @RequestBody request: RegisterDeviceTokenRequest
    ) {
        val userId = UUID.fromString(authentication.name)
        notificationService.registerDeviceToken(userId, request)
    }

    @GetMapping
    fun getNotifications(
        authentication: Authentication,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<NotificationResponse>> {
        val userId = UUID.fromString(authentication.name)
        val response = notificationService.getUserNotifications(userId, pageable)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{notificationId}/read")
    fun markAsRead(
        authentication: Authentication,
        @PathVariable notificationId: UUID
    ): ResponseEntity<NotificationResponse> {
        val userId = UUID.fromString(authentication.name)
        val response = notificationService.markAsRead(userId, notificationId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/unread-count")
    fun getUnreadCount(authentication: Authentication): ResponseEntity<UnreadCountResponse> {
        val userId = UUID.fromString(authentication.name)
        val response = notificationService.getUnreadCount(userId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/admin/send")
    @PreAuthorize("hasRole('ADMIN')")
    fun sendNotification(
        @Valid @RequestBody request: SendNotificationRequest
    ): ResponseEntity<NotificationResponse> {
        val response = notificationService.sendNotification(request)
        return ResponseEntity.ok(response)
    }
}