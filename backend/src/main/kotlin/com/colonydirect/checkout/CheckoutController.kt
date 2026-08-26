package com.colonydirect.checkout

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/checkout")
class CheckoutController(
    private val checkoutService: CheckoutService
) {

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    fun addAddress(
        authentication: Authentication,
        @Valid @RequestBody request: AddressRequest
    ): AddressResponse {
        val userId = UUID.fromString(authentication.name)
        return checkoutService.addAddress(userId, request)
    }

    @GetMapping("/addresses")
    fun getAddresses(authentication: Authentication): List<AddressResponse> {
        val userId = UUID.fromString(authentication.name)
        return checkoutService.getUserAddresses(userId)
    }

    @PostMapping("/process")
    fun processCheckout(
        authentication: Authentication,
        @Valid @RequestBody request: CheckoutRequest
    ): CheckoutResponse {
        val userId = UUID.fromString(authentication.name)
        return checkoutService.processCheckout(userId, request)
    }
}