package com.colonydirect.cart

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/cart")
class CartController(
    private val cartService: CartService
) {
    @GetMapping
    fun getCart(
        authentication: Authentication?,
        @RequestHeader(value = "X-Session-ID", required = false) sessionId: String?
    ): CartResponse {
        val userId = resolveUserId(authentication)
        return cartService.getActiveCart(userId, sessionId)
    }

    @PostMapping("/items")
    fun addItemToCart(
        authentication: Authentication?,
        @RequestHeader(value = "X-Session-ID", required = false) sessionId: String?,
        @Valid @RequestBody request: AddToCartRequest
    ): CartResponse {
        val userId = resolveUserId(authentication)
        return cartService.addItemToCart(userId, sessionId, request)
    }

    @PutMapping("/items/{itemId}")
    fun updateCartItem(
        @PathVariable itemId: UUID,
        @Valid @RequestBody request: UpdateCartItemRequest
    ): CartResponse {
        return cartService.updateCartItem(itemId, request)
    }

    @DeleteMapping("/items/{itemId}")
    fun removeCartItem(@PathVariable itemId: UUID): CartResponse {
        return cartService.removeCartItem(itemId)
    }

    @DeleteMapping
    fun clearCart(
        authentication: Authentication?,
        @RequestHeader(value = "X-Session-ID", required = false) sessionId: String?
    ): ResponseEntity<Void> {
        val userId = resolveUserId(authentication)
        cartService.clearCart(userId, sessionId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/merge")
    fun mergeCart(
        authentication: Authentication,
        @RequestHeader(value = "X-Session-ID") sessionId: String
    ): ResponseEntity<Void> {
        val userId = UUID.fromString(authentication.name)
        cartService.mergeGuestCartToUser(sessionId, userId)
        return ResponseEntity.ok().build()
    }

    private fun resolveUserId(authentication: Authentication?): UUID? {
        return if (authentication?.isAuthenticated == true) {
            UUID.fromString(authentication.name)
        } else {
            null
        }
    }
}