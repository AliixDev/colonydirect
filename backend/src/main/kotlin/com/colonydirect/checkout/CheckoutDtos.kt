package com.colonydirect.checkout

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.UUID

data class AddressRequest(
    @field:NotBlank val fullName: String,
    @field:NotBlank val phoneNumber: String,
    @field:NotBlank val colony: String,
    val block: String?,
    val street: String?,
    @field:NotBlank val houseNumber: String,
    val deliveryInstructions: String?,
    val isDefault: Boolean = false,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null
)

data class AddressResponse(
    val id: UUID,
    val fullName: String,
    val phoneNumber: String,
    val colony: String,
    val block: String?,
    val street: String?,
    val houseNumber: String,
    val deliveryInstructions: String?,
    val isDefault: Boolean
)

enum class PaymentMethod {
    COD, ONLINE
}

data class CheckoutRequest(
    @field:NotNull val addressId: UUID,
    @field:NotNull val paymentMethod: PaymentMethod
)

data class CheckoutResponse(
    val orderId: UUID,
    val orderStatus: String,
    val totalAmount: BigDecimal,
    val paymentMethod: PaymentMethod,
    val requiresPaymentGateway: Boolean
)