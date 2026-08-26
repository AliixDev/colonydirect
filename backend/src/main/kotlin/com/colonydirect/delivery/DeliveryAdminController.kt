package com.colonydirect.delivery

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/deliveries")
@PreAuthorize("hasRole('ADMIN')")
class DeliveryAdminController(
    private val deliveryService: DeliveryService
) {

    @PostMapping("/riders")
    @ResponseStatus(HttpStatus.CREATED)
    fun createRiderProfile(@Valid @RequestBody request: RiderProfileCreateRequest): RiderProfileResponse {
        return deliveryService.createRiderProfile(request)
    }

    @GetMapping("/riders/available")
    fun getAvailableRiders(): List<RiderProfileResponse> {
        return deliveryService.getAvailableRiders()
    }

    @PostMapping("/order/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun initializeDelivery(@PathVariable orderId: UUID): DeliveryResponse {
        return deliveryService.initializeDelivery(orderId)
    }

    @PutMapping("/{deliveryId}/assign")
    fun assignDelivery(
        @PathVariable deliveryId: UUID,
        @Valid @RequestBody request: DeliveryAssignmentRequest
    ): DeliveryResponse {
        return deliveryService.assignDelivery(deliveryId, request)
    }
}