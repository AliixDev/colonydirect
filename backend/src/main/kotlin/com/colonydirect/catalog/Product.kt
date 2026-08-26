package com.colonydirect.catalog

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

enum class ProductType {
    FRESH_MARKET,
    GROCERY_SERVICE
}

@Entity
@Table(name = "products")
class Product(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "category_id", nullable = false)
    var categoryId: UUID,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, length = 280, unique = true)
    var slug: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    var productType: ProductType = ProductType.FRESH_MARKET,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)