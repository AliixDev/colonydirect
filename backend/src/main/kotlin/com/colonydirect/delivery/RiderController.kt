package com.colonydirect.delivery

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/rider")
@PreAuthorize("hasRole('RIDER')")
class RiderController(
    private val deliveryService: DeliveryService
) {

    @PutMapping("/location")
    fun updateLocation(
        authentication: Authentication,
        @Valid @RequestBody request: RiderLocationUpdateRequest
    ): RiderProfileResponse {
        val userId = UUID.fromString(authentication.name)
        return deliveryService.updateRiderLocation(userId, request)
    }

    @PutMapping("/deliveries/{deliveryId}/status")
    fun updateDeliveryStatus(
        authentication: Authentication,
        @PathVariable deliveryId: UUID,
        @Valid @RequestBody request: DeliveryStatusUpdateRequest
    ): DeliveryResponse {
        val userId = UUID.fromString(authentication.name)
        return deliveryService.updateDeliveryStatus(userId, deliveryId, request)
    }

    @GetMapping("/deliveries")
    fun getMyDeliveries(
        authentication: Authentication,
        @RequestParam(required = false) statuses: List<DeliveryStatus>?,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<DeliveryResponse> {
        val userId = UUID.fromString(authentication.name)
        val targetStatuses = statuses ?: DeliveryStatus.entries.toList()
        return deliveryService.getRiderDeliveries(userId, targetStatuses, pageable)
    }
}