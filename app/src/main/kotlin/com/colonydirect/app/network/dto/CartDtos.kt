package com.colonydirect.app.network.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

// ─── Cart DTOs (mirrors backend CartDtos.kt) ─────────────────────────────────

data class AddToCartRequest(
    @SerializedName("productId") val productId: String,
    @SerializedName("variantId") val variantId: String? = null,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("customInstruction") val customInstruction: String? = null
)

data class UpdateCartItemRequest(
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("customInstruction") val customInstruction: String? = null
)

data class CartItemResponse(
    @SerializedName("id") val id: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("variantId") val variantId: String?,
    @SerializedName("productName") val productName: String,
    @SerializedName("variantName") val variantName: String?,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unitPrice") val unitPrice: BigDecimal,
    @SerializedName("subtotal") val subtotal: BigDecimal,
    @SerializedName("customInstruction") val customInstruction: String?,
    @SerializedName("imageUrl") val imageUrl: String?
)

data class CartResponse(
    @SerializedName("id") val id: String,
    @SerializedName("items") val items: List<CartItemResponse>,
    @SerializedName("totalAmount") val totalAmount: BigDecimal,
    @SerializedName("totalItems") val totalItems: Int
)
