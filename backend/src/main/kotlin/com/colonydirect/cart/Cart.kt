package com.colonydirect.cart

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class CartStatus {
    ACTIVE,
    CHECKED_OUT,
    ABANDONED
}

@Entity
@Table(name = "carts")
class Cart(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id")
    var userId: UUID? = null,

    @Column(name = "session_id")
    var sessionId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CartStatus = CartStatus.ACTIVE,

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

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