package com.colonydirect.order

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "order_items")
class OrderItem(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "order_id", nullable = false)
    val orderId: UUID,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(name = "variant_id")
    val variantId: UUID? = null,

    @Column(nullable = false)
    val quantity: Int,

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    val unitPrice: BigDecimal,

    @Column(nullable = false, precision = 12, scale = 2)
    val subtotal: BigDecimal,

    @Column(name = "custom_instruction", length = 500)
    val customInstruction: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)