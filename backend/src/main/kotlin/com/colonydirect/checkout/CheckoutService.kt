package com.colonydirect.checkout

import com.colonydirect.cart.CartService
import com.colonydirect.cart.CartStatus
import com.colonydirect.cart.CartRepository
import com.colonydirect.order.Order
import com.colonydirect.order.OrderItem
import com.colonydirect.order.OrderItemRepository
import com.colonydirect.order.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional
class CheckoutService(
    private val addressRepository: UserAddressRepository,
    private val cartService: CartService,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository
) {

    fun addAddress(userId: UUID, request: AddressRequest): AddressResponse {
        val address = addressRepository.save(
            UserAddress(
                userId = userId,
                fullName = request.fullName,
                phoneNumber = request.phoneNumber,
                colony = request.colony,
                block = request.block,
                street = request.street,
                houseNumber = request.houseNumber,
                deliveryInstructions = request.deliveryInstructions,
                isDefault = request.isDefault,
                latitude = request.latitude,
                longitude = request.longitude
            )
        )

        if (request.isDefault) {
            addressRepository.unsetDefaultForOtherAddresses(userId, address.id)
        }

        return address.toResponse()
    }

    @Transactional(readOnly = true)
    fun getUserAddresses(userId: UUID): List<AddressResponse> {
        return addressRepository.findAllByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
            .map { it.toResponse() }
    }

    fun processCheckout(userId: UUID, request: CheckoutRequest): CheckoutResponse {
        // 1. Validate Address
        val address = addressRepository.findByIdAndUserId(request.addressId, userId)
            ?: throw IllegalArgumentException("Invalid delivery address")

        // 2. Fetch and Validate Cart
        val cartResponse = cartService.getActiveCart(userId, null)
        if (cartResponse.items.isEmpty()) {
            throw IllegalStateException("Cart is empty")
        }

        // 3. Create Order
        val deliveryFee = BigDecimal("50.00") // Fixed delivery fee for Colony Direct MVP
        val grandTotal = cartResponse.totalAmount.add(deliveryFee)

        val order = orderRepository.save(
            Order(
                userId = userId,
                totalAmount = cartResponse.totalAmount, // Map to existing domain entity structure
                status = "PENDING"
            )
        )

        // 4. Create Order Items
        cartResponse.items.forEach { cartItem ->
            orderItemRepository.save(
                OrderItem(
                    orderId = order.id,
                    productId = cartItem.productId,
                    variantId = cartItem.variantId,
                    quantity = cartItem.quantity,
                    unitPrice = cartItem.unitPrice,
                    subtotal = cartItem.subtotal,
                    customInstruction = cartItem.customInstruction
                )
            )
        }

        // 5. Clear Cart (Mark as Checked Out)
        val cart = cartRepository.findById(cartResponse.id).get()
        cart.status = CartStatus.CHECKED_OUT
        cartRepository.save(cart)

        return CheckoutResponse(
            orderId = order.id,
            orderStatus = order.status,
            totalAmount = grandTotal,
            paymentMethod = request.paymentMethod,
            requiresPaymentGateway = request.paymentMethod == PaymentMethod.ONLINE
        )
    }

    private fun UserAddress.toResponse() = AddressResponse(
        id = id,
        fullName = fullName,
        phoneNumber = phoneNumber,
        colony = colony,
        block = block,
        street = street,
        houseNumber = houseNumber,
        deliveryInstructions = deliveryInstructions,
        isDefault = isDefault
    )
}