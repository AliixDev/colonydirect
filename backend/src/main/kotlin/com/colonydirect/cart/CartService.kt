package com.colonydirect.cart

import com.colonydirect.catalog.ProductRepository
import com.colonydirect.catalog.ProductVariantRepository
import com.colonydirect.catalog.ProductImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val productVariantRepository: ProductVariantRepository,
    private val productImageRepository: ProductImageRepository
) {
    fun getActiveCart(userId: UUID?, sessionId: String?): CartResponse {
        require(userId != null || sessionId != null) { "Must provide either user ID or session ID" }
        
        val cart = resolveCart(userId, sessionId)
        return buildCartResponse(cart)
    }

    fun addItemToCart(userId: UUID?, sessionId: String?, request: AddToCartRequest): CartResponse {
        require(userId != null || sessionId != null) { "Must provide either user ID or session ID" }
        
        // Validate Product and Variant Existence
        val product = productRepository.findById(request.productId)
            .orElseThrow { NoSuchElementException("Product not found") }
        
        if (request.variantId != null) {
            val variant = productVariantRepository.findById(request.variantId)
                .orElseThrow { NoSuchElementException("Variant not found") }
            require(variant.productId == product.id) { "Variant does not belong to product" }
            require(variant.stockQuantity >= request.quantity) { "Insufficient stock" }
        }

        val cart = resolveCart(userId, sessionId)
        
        val existingItem = cartItemRepository.findByCartIdAndProductIdAndVariantId(
            cartId = cart.id,
            productId = request.productId,
            variantId = request.variantId
        )

        if (existingItem != null) {
            existingItem.quantity += request.quantity
            request.customInstruction?.let { existingItem.customInstruction = it }
            cartItemRepository.save(existingItem)
        } else {
            cartItemRepository.save(
                CartItem(
                    cartId = cart.id,
                    productId = request.productId,
                    variantId = request.variantId,
                    quantity = request.quantity,
                    customInstruction = request.customInstruction
                )
            )
        }

        return buildCartResponse(cart)
    }

    fun updateCartItem(cartItemId: UUID, request: UpdateCartItemRequest): CartResponse {
        val item = cartItemRepository.findById(cartItemId)
            .orElseThrow { NoSuchElementException("Cart item not found") }
            
        if (item.variantId != null) {
            val variant = productVariantRepository.findById(item.variantId)
                .orElseThrow { NoSuchElementException("Variant not found") }
            require(variant.stockQuantity >= request.quantity) { "Insufficient stock" }
        }
        
        item.quantity = request.quantity
        request.customInstruction?.let { item.customInstruction = it }
        cartItemRepository.save(item)
        
        val cart = cartRepository.findById(item.cartId).get()
        return buildCartResponse(cart)
    }

    fun removeCartItem(cartItemId: UUID): CartResponse {
        val item = cartItemRepository.findById(cartItemId)
            .orElseThrow { NoSuchElementException("Cart item not found") }
        val cart = cartRepository.findById(item.cartId).get()
        
        cartItemRepository.delete(item)
        return buildCartResponse(cart)
    }

    fun clearCart(userId: UUID?, sessionId: String?) {
        val cart = resolveCart(userId, sessionId)
        cartItemRepository.deleteAllByCartId(cart.id)
        cart.totalAmount = BigDecimal.ZERO
        cartRepository.save(cart)
    }

    fun mergeGuestCartToUser(sessionId: String, userId: UUID) {
        val guestCart = cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE) ?: return
        val userCart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
        
        if (userCart == null) {
            guestCart.userId = userId
            guestCart.sessionId = null
            cartRepository.save(guestCart)
        } else {
            val guestItems = cartItemRepository.findAllByCartId(guestCart.id)
            for (item in guestItems) {
                addItemToCart(userId, null, AddToCartRequest(
                    productId = item.productId,
                    variantId = item.variantId,
                    quantity = item.quantity,
                    customInstruction = item.customInstruction
                ))
            }
            cartRepository.delete(guestCart)
        }
    }

    private fun resolveCart(userId: UUID?, sessionId: String?): Cart {
        if (userId != null) {
            return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                ?: cartRepository.save(Cart(userId = userId))
        }
        return cartRepository.findBySessionIdAndStatus(sessionId!!, CartStatus.ACTIVE)
            ?: cartRepository.save(Cart(sessionId = sessionId))
    }

    private fun buildCartResponse(cart: Cart): CartResponse {
        val items = cartItemRepository.findAllByCartId(cart.id)
        var cartTotal = BigDecimal.ZERO
        var totalItems = 0

        val itemResponses = items.map { item ->
            val product = productRepository.findById(item.productId).get()
            var unitPrice = BigDecimal.ZERO
            var variantName: String? = null
            var imageUrl: String? = null

            if (item.variantId != null) {
                val variant = productVariantRepository.findById(item.variantId).get()
                unitPrice = variant.discountPriceAmount ?: variant.priceAmount
                variantName = variant.variantName
                val images = productImageRepository.findAllByVariantId(item.variantId)
                imageUrl = images.firstOrNull { it.isPrimary }?.imageUrl ?: images.firstOrNull()?.imageUrl
            }
            
            if (imageUrl == null) {
                val pImages = productImageRepository.findAllByProductIdOrderByDisplayOrderAsc(product.id)
                imageUrl = pImages.firstOrNull { it.isPrimary }?.imageUrl ?: pImages.firstOrNull()?.imageUrl
            }

            val subtotal = unitPrice.multiply(BigDecimal(item.quantity))
            cartTotal = cartTotal.add(subtotal)
            totalItems += item.quantity

            CartItemResponse(
                id = item.id,
                productId = item.productId,
                variantId = item.variantId,
                productName = product.name,
                variantName = variantName,
                quantity = item.quantity,
                unitPrice = unitPrice,
                subtotal = subtotal,
                customInstruction = item.customInstruction,
                imageUrl = imageUrl
            )
        }

        cart.totalAmount = cartTotal
        cartRepository.save(cart)

        return CartResponse(
            id = cart.id,
            items = itemResponses,
            totalAmount = cartTotal,
            totalItems = totalItems
        )
    }
}