package com.colonydirect.delivery

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

enum class DeliveryStatus {
    PENDING,
    ASSIGNED,
    PICKED_UP,
    DELIVERED,
    FAILED
}

@Entity
@Table(name = "deliveries")
class Delivery(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "order_id", nullable = false, unique = true)
    val orderId: UUID,

    @Column(name = "rider_id")
    var riderId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DeliveryStatus = DeliveryStatus.PENDING,

    @Column(name = "pickup_time")
    var pickupTime: Instant? = null,

    @Column(name = "delivery_time")
    var deliveryTime: Instant? = null,

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    var deliveryNotes: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}