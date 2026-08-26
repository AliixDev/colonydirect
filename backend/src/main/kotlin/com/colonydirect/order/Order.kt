package com.colonydirect.order

import com.colonydirect.common.Money
import java.time.Instant
import java.util.UUID

enum class OrderType { FRESH_MARKET, GROCERY_DELIVERY }

enum class OrderStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    PENDING_ADMIN_REVIEW,
    OUT_FOR_DELIVERY,
    DELIVERED,
    PAYMENT_FAILED,
    UNFULFILLED,
    PARTIALLY_FULFILLED,
    CANCELLED
}

enum class PaymentMethod { COD, EASYPAISA, JAZZCASH, BANK_TRANSFER }

data class OrderItem(
    val id: UUID = UUID.randomUUID(),
    val productVariantId: UUID?,
    val freeformDescription: String?,
    val quantity: Int,
    val unitPrice: Money?
) {
    init {
        require(quantity > 0) { "Order item quantity must be positive" }
        require(productVariantId != null || !freeformDescription.isNullOrBlank()) {
            "Order item must reference a catalog variant or provide a freeform description"
        }
    }
}

/**
 * Polymorphic order aggregate covering both Fresh Market and Grocery Delivery,
 * per Phase 8 schema design (single orders table, order_type discriminator).
 */
data class Order(
    val id: UUID = UUID.randomUUID(),
    val orderType: OrderType,
    val customerId: UUID,
    val colonyId: UUID,
    val items: List<OrderItem>,
    var status: OrderStatus = OrderStatus.PENDING_PAYMENT,
    val deliveryFee: Money,
    // Grocery Delivery only fields (null for Fresh Market)
    val storeName: String? = null,
    val estimatedCeiling: Money? = null,
    var actualTotal: Money? = null,
    var receiptPhotoUrl: String? = null,
    val createdAt: Instant = Instant.now()
) {
    init {
        require(items.isNotEmpty()) { "Order must contain at least one item" }
        if (orderType == OrderType.GROCERY_DELIVERY) {
            require(!storeName.isNullOrBlank()) { "Grocery Delivery orders require a store name" }
            require(estimatedCeiling != null) { "Grocery Delivery orders require a computed estimated ceiling" }
        }
    }

    /** Fresh Market subtotal from catalog-priced items only; Grocery Delivery uses actualTotal instead. */
    fun catalogSubtotal(): Money =
        items.fold(Money.ZERO) { acc, item ->
            val price = item.unitPrice ?: Money.ZERO
            acc + (price * item.quantity)
        }

    companion object {
        /** FR-PAY-2: Grocery Delivery orders must never allow COD, enforced at construction time. */
        fun validatePaymentMethod(orderType: OrderType, method: PaymentMethod) {
            if (orderType == OrderType.GROCERY_DELIVERY && method == PaymentMethod.COD) {
                throw IllegalArgumentException("COD_NOT_ALLOWED_FOR_ORDER_TYPE")
            }
        }
    }
}
