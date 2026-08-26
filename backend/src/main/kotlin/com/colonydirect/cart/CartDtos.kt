package com.colonydirect.cart

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.UUID

data class AddToCartRequest(
    @field:NotNull val productId: UUID,
    val variantId: UUID? = null,
    @field:Min(1) val quantity: Int,
    val customInstruction: String? = null
)

data class UpdateCartItemRequest(
    @field:Min(1) val quantity: Int,
    val customInstruction: String? = null
)

data class CartItemResponse(
    val id: UUID,
    val productId: UUID,
    val variantId: UUID?,
    val productName: String,
    val variantName: String?,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
    val customInstruction: String?,
    val imageUrl: String?
)

data class CartResponse(
    val id: UUID,
    val items: List<CartItemResponse>,
    val totalAmount: BigDecimal,
    val totalItems: Int
)