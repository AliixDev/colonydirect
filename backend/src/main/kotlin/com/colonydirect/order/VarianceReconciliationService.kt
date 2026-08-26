package com.colonydirect.order

import com.colonydirect.common.Money
import com.colonydirect.payment.PaymentGateway
import com.colonydirect.payment.PaymentResult
import org.springframework.stereotype.Service

/**
 * Implements the Grocery Delivery reconciliation workflow from Phase 4, Section 4.2:
 * rider enters the actual receipt total; if it's within the configured variance band
 * of the pre-authorized ceiling, payment is auto-captured. Outside the band, the order
 * is routed to PENDING_ADMIN_REVIEW instead of being silently captured or silently rejected.
 *
 * Default variance band is 15%, per Phase 2 Section 0 Decision 1, but is passed in as a
 * parameter here since it's a colony-scoped, admin-configurable setting (Phase 7 cross-cutting
 * concern: config, not code constants).
 */
@Service
class VarianceReconciliationService(
    private val paymentGateway: PaymentGateway,
    private val orderRepository: OrderRepository,
    private val auditLog: OrderAuditLog
) {

    /**
     * Called from the rider "purchase-complete" endpoint (POST /rider/orders/{id}/purchase-complete).
     * Returns the resulting order status so the API layer can respond appropriately; never
     * throws for a legitimate over-variance case — that's an expected business outcome, not an error.
     */
    fun reconcile(
        order: Order,
        actualTotal: Money,
        receiptPhotoUrl: String,
        variancePercent: Int
    ): ReconciliationOutcome {
        require(order.orderType == OrderType.GROCERY_DELIVERY) {
            "Variance reconciliation only applies to Grocery Delivery orders"
        }
        val ceiling = order.estimatedCeiling
            ?: error("Grocery Delivery order ${order.id} is missing its estimated ceiling — data integrity violation")

        order.actualTotal = actualTotal
        order.receiptPhotoUrl = receiptPhotoUrl

        return if (actualTotal.isWithinVariance(ceiling, variancePercent)) {
            val captureAmount = actualTotal + order.deliveryFee
            when (val result = paymentGateway.capture(order.id, captureAmount)) {
                is PaymentResult.Success -> {
                    order.status = OrderStatus.OUT_FOR_DELIVERY
                    orderRepository.save(order)
                    auditLog.recordAutoCapture(order.id, ceiling, actualTotal, result.gatewayReference)
                    ReconciliationOutcome.AutoCaptured(order.id, captureAmount)
                }
                is PaymentResult.Failure -> {
                    order.status = OrderStatus.PAYMENT_FAILED
                    orderRepository.save(order)
                    auditLog.recordCaptureFailure(order.id, result.reason)
                    ReconciliationOutcome.CaptureFailed(order.id, result.reason)
                }
            }
        } else {
            order.status = OrderStatus.PENDING_ADMIN_REVIEW
            orderRepository.save(order)
            auditLog.recordFlaggedForReview(order.id, ceiling, actualTotal)
            ReconciliationOutcome.FlaggedForReview(order.id, ceiling, actualTotal)
        }
    }

    /**
     * Admin decision from the variance-review queue (Phase 6, Section 6.7).
     * Every branch is explicit; there is no default/fallthrough path that silently
     * captures or silently cancels money without an admin's explicit choice.
     */
    fun applyAdminDecision(order: Order, decision: AdminVarianceDecision): ReconciliationOutcome {
        require(order.status == OrderStatus.PENDING_ADMIN_REVIEW) {
            "Order ${order.id} is not awaiting variance review (status=${order.status})"
        }
        val actualTotal = order.actualTotal
            ?: error("Order ${order.id} in PENDING_ADMIN_REVIEW has no actual total recorded — data integrity violation")

        return when (decision) {
            is AdminVarianceDecision.Approve -> {
                val captureAmount = actualTotal + order.deliveryFee
                when (val result = paymentGateway.capture(order.id, captureAmount)) {
                    is PaymentResult.Success -> {
                        order.status = OrderStatus.OUT_FOR_DELIVERY
                        orderRepository.save(order)
                        auditLog.recordAdminApprovedCapture(order.id, decision.adminId, result.gatewayReference)
                        ReconciliationOutcome.AutoCaptured(order.id, captureAmount)
                    }
                    is PaymentResult.Failure -> {
                        order.status = OrderStatus.PAYMENT_FAILED
                        orderRepository.save(order)
                        ReconciliationOutcome.CaptureFailed(order.id, result.reason)
                    }
                }
            }
            is AdminVarianceDecision.Cancel -> {
                paymentGateway.refundPreAuth(order.id)
                order.status = OrderStatus.CANCELLED
                orderRepository.save(order)
                auditLog.recordAdminCancelled(order.id, decision.adminId, decision.reason)
                ReconciliationOutcome.Cancelled(order.id, decision.reason)
            }
        }
    }
}

sealed class ReconciliationOutcome {
    data class AutoCaptured(val orderId: java.util.UUID, val amount: Money) : ReconciliationOutcome()
    data class CaptureFailed(val orderId: java.util.UUID, val reason: String) : ReconciliationOutcome()
    data class FlaggedForReview(val orderId: java.util.UUID, val ceiling: Money, val actual: Money) : ReconciliationOutcome()
    data class Cancelled(val orderId: java.util.UUID, val reason: String) : ReconciliationOutcome()
}

sealed class AdminVarianceDecision {
    data class Approve(val adminId: java.util.UUID) : AdminVarianceDecision()
    data class Cancel(val adminId: java.util.UUID, val reason: String) : AdminVarianceDecision()
}

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: java.util.UUID): Order?
}

interface OrderAuditLog {
    fun recordAutoCapture(orderId: java.util.UUID, ceiling: Money, actual: Money, gatewayRef: String)
    fun recordCaptureFailure(orderId: java.util.UUID, reason: String)
    fun recordFlaggedForReview(orderId: java.util.UUID, ceiling: Money, actual: Money)
    fun recordAdminApprovedCapture(orderId: java.util.UUID, adminId: java.util.UUID, gatewayRef: String)
    fun recordAdminCancelled(orderId: java.util.UUID, adminId: java.util.UUID, reason: String)
}
