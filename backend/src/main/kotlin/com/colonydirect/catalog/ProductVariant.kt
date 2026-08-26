package com.colonydirect.catalog

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class MeasurementUnit {
    KG, GRAM, LITER, MILLILITER, PIECE, PACK
}

@Entity
@Table(name = "product_variants")
class ProductVariant(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "product_id", nullable = false)
    val productId: UUID,

    @Column(nullable = false, unique = true)
    var sku: String,

    @Column(name = "variant_name", nullable = false)
    var variantName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var unit: MeasurementUnit,

    @Column(name = "unit_value", nullable = false, precision = 12, scale = 4)
    var unitValue: BigDecimal,

    @Column(name = "price_amount", nullable = false, precision = 12, scale = 2)
    var priceAmount: BigDecimal,

    @Column(name = "price_currency", nullable = false, length = 3)
    var priceCurrency: String = "PKR",

    @Column(name = "discount_price_amount", precision = 12, scale = 2)
    var discountPriceAmount: BigDecimal? = null,

    @Column(name = "stock_quantity", nullable = false)
    var stockQuantity: Int = 0,

    @Column(nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)