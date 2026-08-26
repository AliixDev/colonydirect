package com.colonydirect.ai

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_product_frequencies")
class UserProductFrequency(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(name = "variant_id")
    val variantId: UUID? = null,

    @Column(name = "purchase_count", nullable = false)
    var purchaseCount: Int = 1,

    @Column(name = "last_purchased_at", nullable = false)
    var lastPurchasedAt: Instant = Instant.now(),

    @Column(name = "average_interval_days", nullable = false)
    var averageIntervalDays: Int = 7
)