package com.colonydirect.notification

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

enum class NotificationType {
    ORDER_UPDATE,
    PROMOTION,
    SYSTEM
}

@Entity
@Table(name = "notifications")
class Notification(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(nullable = false, length = 200)
    val title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val body: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val type: NotificationType = NotificationType.SYSTEM,

    @Column(name = "reference_id")
    val referenceId: UUID? = null,

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)