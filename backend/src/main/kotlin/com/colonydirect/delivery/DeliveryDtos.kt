package com.colonydirect.delivery

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class RiderProfileCreateRequest(
    @field:NotNull val userId: UUID,
    @field:NotBlank val vehicleType: String,
    val licenseNumber: String? = null
)

data class RiderProfileResponse(
    val id: UUID,
    val userId: UUID,
    val vehicleType: String,
    val licenseNumber: String?,
    val isAvailable: Boolean,
    val currentLatitude: BigDecimal?,
    val currentLongitude: BigDecimal?
)

data class RiderLocationUpdateRequest(
    @field:NotNull val latitude: BigDecimal,
    @field:NotNull val longitude: BigDecimal,
    val isAvailable: Boolean? = null
)

data class DeliveryAssignmentRequest(
    @field:NotNull val riderId: UUID
)

data class DeliveryStatusUpdateRequest(
    @field:NotNull val status: DeliveryStatus,
    val notes: String? = null
)

data class DeliveryResponse(
    val id: UUID,
    val orderId: UUID,
    val riderId: UUID?,
    val status: DeliveryStatus,
    val pickupTime: Instant?,
    val deliveryTime: Instant?,
    val deliveryNotes: String?
)