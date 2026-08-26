package com.colonydirect.app.network.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

// ─── Address DTOs ─────────────────────────────────────────────────────────────

data class AddressRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("colony") val colony: String,
    @SerializedName("block") val block: String?,
    @SerializedName("street") val street: String?,
    @SerializedName("houseNumber") val houseNumber: String,
    @SerializedName("deliveryInstructions") val deliveryInstructions: String?,
    @SerializedName("isDefault") val isDefault: Boolean = false
)

data class AddressResponse(
    @SerializedName("id") val id: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("colony") val colony: String,
    @SerializedName("block") val block: String?,
    @SerializedName("street") val street: String?,
    @SerializedName("houseNumber") val houseNumber: String,
    @SerializedName("deliveryInstructions") val deliveryInstructions: String?,
    @SerializedName("isDefault") val isDefault: Boolean
)

// ─── Checkout DTOs ────────────────────────────────────────────────────────────

data class CheckoutRequest(
    @SerializedName("addressId") val addressId: String,
    @SerializedName("paymentMethod") val paymentMethod: String   // "COD" | "ONLINE"
)

data class CheckoutResponse(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("orderStatus") val orderStatus: String,
    @SerializedName("totalAmount") val totalAmount: BigDecimal,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("requiresPaymentGateway") val requiresPaymentGateway: Boolean
)
