package com.colonydirect.order

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * FR-PAY-2: Grocery Delivery orders must never allow COD. Enforced at the domain
 * layer (not just hidden in UI) so a future API client or admin tool cannot bypass it.
 */
class OrderPaymentPolicyTest {

    @Test
    fun `COD is rejected for Grocery Delivery orders`() {
        assertThrows(IllegalArgumentException::class.java) {
            Order.validatePaymentMethod(OrderType.GROCERY_DELIVERY, PaymentMethod.COD)
        }
    }

    @Test
    fun `COD is allowed for Fresh Market orders`() {
        assertDoesNotThrow {
            Order.validatePaymentMethod(OrderType.FRESH_MARKET, PaymentMethod.COD)
        }
    }

    @Test
    fun `digital payment methods are allowed for both order types`() {
        assertDoesNotThrow {
            Order.validatePaymentMethod(OrderType.GROCERY_DELIVERY, PaymentMethod.EASYPAISA)
            Order.validatePaymentMethod(OrderType.FRESH_MARKET, PaymentMethod.JAZZCASH)
        }
    }
}
