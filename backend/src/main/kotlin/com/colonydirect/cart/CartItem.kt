package com.colonydirect.cart

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "cart_items")
class CartItem(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "cart_id", nullable = false)
    val cartId: UUID,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(name = "variant_id")
    val variantId: UUID? = null,

    @Column(nullable = false)
    var quantity: Int,

    @Column(name = "custom_instruction", length = 500)
    var customInstruction: String? = null,

    @Column(name = "added_at", nullable = false, updatable = false)
    val addedAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}