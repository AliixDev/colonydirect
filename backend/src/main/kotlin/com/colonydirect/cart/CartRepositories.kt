package com.colonydirect.cart

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface CartRepository : JpaRepository<Cart, UUID> {
    fun findByUserIdAndStatus(userId: UUID, status: CartStatus): Cart?
    fun findBySessionIdAndStatus(sessionId: String, status: CartStatus): Cart?
}

interface CartItemRepository : JpaRepository<CartItem, UUID> {
    fun findAllByCartId(cartId: UUID): List<CartItem>
    fun findByCartIdAndProductIdAndVariantId(cartId: UUID, productId: UUID, variantId: UUID?): CartItem?
    
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.cartId = :cartId")
    fun deleteAllByCartId(cartId: UUID)
}