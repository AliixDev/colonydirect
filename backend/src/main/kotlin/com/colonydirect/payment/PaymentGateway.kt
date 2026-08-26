package com.colonydirect.payment

import com.colonydirect.common.Money
import java.util.UUID

sealed class PaymentResult {
    data class Success(val gatewayReference: String) : PaymentResult()
    data class Failure(val reason: String) : PaymentResult()
}

/**
 * Abstraction over EasyPaisa/JazzCash/bank transfer so the Order/Variance services
 * (Phase 10) never depend on a specific gateway SDK directly (Phase 7 architecture:
 * payment methods are data-driven and swappable). Every capture call MUST be idempotent
 * per orderId — retried network calls from poor mobile connectivity must never double-charge.
 */
interface PaymentGateway {
    fun capture(orderId: UUID, amount: Money): PaymentResult
    fun refundPreAuth(orderId: UUID): PaymentResult
}

/**
 * In-memory idempotent gateway used for local development and integration tests.
 * Production implementations (EasyPaisaGateway, JazzCashGateway) wrap the real
 * merchant APIs but share this same idempotency contract.
 */
class IdempotentInMemoryPaymentGateway : PaymentGateway {
    private val capturedByOrder = mutableMapOf<UUID, PaymentResult.Success>()

    override fun capture(orderId: UUID, amount: Money): PaymentResult {
        // Idempotency: a retry for an order already captured returns the same result
        // rather than charging again.
        capturedByOrder[orderId]?.let { return it }
        val result = PaymentResult.Success(gatewayReference = "SIM-${orderId}-${amount.minorUnits}")
        capturedByOrder[orderId] = result
        return result
    }

    override fun refundPreAuth(orderId: UUID): PaymentResult {
        capturedByOrder.remove(orderId)
        return PaymentResult.Success(gatewayReference = "SIM-REFUND-$orderId")
    }
}
