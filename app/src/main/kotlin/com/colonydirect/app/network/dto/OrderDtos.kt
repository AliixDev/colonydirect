package com.colonydirect.app.network.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

// ─── Order DTOs ───────────────────────────────────────────────────────────────

data class OrderItemResponse(
    @SerializedName("id") val id: String,
    @SerializedName("productName") val productName: String?,
    @SerializedName("variantName") val variantName: String?,
    @SerializedName("freeformDescription") val freeformDescription: String?,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unitPrice") val unitPrice: BigDecimal?,
    @SerializedName("subtotal") val subtotal: BigDecimal?
)

data class OrderDetailResponse(
    @SerializedName("id") val id: String,
    @SerializedName("orderType") val orderType: String,
    @SerializedName("status") val status: String,
    @SerializedName("totalAmount") val totalAmount: BigDecimal,
    @SerializedName("deliveryFee") val deliveryFee: BigDecimal,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("items") val items: List<OrderItemResponse>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("deliveryAddress") val deliveryAddress: AddressResponse?
)

data class OrderSummaryResponse(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("status") val status: String,
    @SerializedName("totalAmount") val totalAmount: BigDecimal,
    @SerializedName("itemCount") val itemCount: Int,
    @SerializedName("createdAt") val createdAt: String
)

data class PagedOrderResponse(
    @SerializedName("content") val content: List<OrderSummaryResponse>,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("number") val number: Int,
    @SerializedName("last") val last: Boolean
)
