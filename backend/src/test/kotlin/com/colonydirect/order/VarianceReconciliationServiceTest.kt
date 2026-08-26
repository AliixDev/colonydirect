package com.colonydirect.order

import com.colonydirect.common.Money
import com.colonydirect.payment.IdempotentInMemoryPaymentGateway
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

private class InMemoryOrderRepository : OrderRepository {
    private val store = mutableMapOf<UUID, Order>()
    override fun save(order: Order): Order { store[order.id] = order; return order }
    override fun findById(id: UUID): Order? = store[id]
}

private class RecordingAuditLog : OrderAuditLog {
    val events = mutableListOf<String>()
    override fun recordAutoCapture(orderId: UUID, ceiling: Money, actual: Money, gatewayRef: String) { events += "AUTO_CAPTURE" }
    override fun recordCaptureFailure(orderId: UUID, reason: String) { events += "CAPTURE_FAILED" }
    override fun recordFlaggedForReview(orderId: UUID, ceiling: Money, actual: Money) { events += "FLAGGED" }
    override fun recordAdminApprovedCapture(orderId: UUID, adminId: UUID, gatewayRef: String) { events += "ADMIN_APPROVED" }
    override fun recordAdminCancelled(orderId: UUID, adminId: UUID, reason: String) { events += "ADMIN_CANCELLED" }
}

class VarianceReconciliationServiceTest {

    private lateinit var service: VarianceReconciliationService
    private lateinit var repository: InMemoryOrderRepository
    private lateinit var auditLog: RecordingAuditLog
    private lateinit var gateway: IdempotentInMemoryPaymentGateway

    private fun groceryOrder(ceiling: Money): Order = Order(
        orderType = OrderType.GROCERY_DELIVERY,
        customerId = UUID.randomUUID(),
        colonyId = UUID.randomUUID(),
        items = listOf(OrderItem(productVariantId = null, freeformDescription = "2 Coke, 1 Bread", quantity = 1, unitPrice = null)),
        deliveryFee = Money.ofRupees(50),
        storeName = "Fateh Mart",
        estimatedCeiling = ceiling
    )

    @BeforeEach
    fun setup() {
        gateway = IdempotentInMemoryPaymentGateway()
        repository = InMemoryOrderRepository()
        auditLog = RecordingAuditLog()
        service = VarianceReconciliationService(gateway, repository, auditLog)
    }

    @Test
    fun `actual total within variance band auto-captures and marks OUT_FOR_DELIVERY`() {
        val order = groceryOrder(ceiling = Money.ofRupees(1000))
        val actual = Money.ofRupees(1080) // 8% over — within default 15% band

        val outcome = service.reconcile(order, actual, "https://receipts/r1.jpg", variancePercent = 15)

        assertTrue(outcome is ReconciliationOutcome.AutoCaptured)
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.status)
        assertEquals(listOf("AUTO_CAPTURE"), auditLog.events)
    }

    @Test
    fun `actual total beyond variance band is flagged for admin review, not auto-captured`() {
        val order = groceryOrder(ceiling = Money.ofRupees(1000))
        val actual = Money.ofRupees(1400) // 40% over — outside default 15% band

        val outcome = service.reconcile(order, actual, "https://receipts/r2.jpg", variancePercent = 15)

        assertTrue(outcome is ReconciliationOutcome.FlaggedForReview)
        assertEquals(OrderStatus.PENDING_ADMIN_REVIEW, order.status)
        assertEquals(listOf("FLAGGED"), auditLog.events)
    }

    @Test
    fun `exact boundary of variance band is treated as within band (inclusive)`() {
        val order = groceryOrder(ceiling = Money.ofRupees(1000))
        val actual = Money.ofRupees(1150) // exactly 15% over

        val outcome = service.reconcile(order, actual, "https://receipts/r3.jpg", variancePercent = 15)

        assertTrue(outcome is ReconciliationOutcome.AutoCaptured)
    }

    @Test
    fun `admin approve on a flagged order captures payment and unblocks delivery`() {
        val order = groceryOrder(ceiling = Money.ofRupees(1000))
        service.reconcile(order, Money.ofRupees(1400), "https://receipts/r4.jpg", variancePercent = 15)
        assertEquals(OrderStatus.PENDING_ADMIN_REVIEW, order.status)

        val outcome = service.applyAdminDecision(order, AdminVarianceDecision.Approve(adminId = UUID.randomUUID()))

        assertTrue(outcome is ReconciliationOutcome.AutoCaptured)
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.status)
    }

    @Test
    fun `admin cancel on a flagged order refunds pre-auth and cancels order`() {
        val order = groceryOrder(ceiling = Money.ofRupees(1000))
        service.reconcile(order, Money.ofRupees(1400), "https://receipts/r5.jpg", variancePercent = 15)

        val outcome = service.applyAdminDecision(
            order,
            AdminVarianceDecision.Cancel(adminId = UUID.randomUUID(), reason = "Customer unreachable")
        )

        assertTrue(outcome is ReconciliationOutcome.Cancelled)
        assertEquals(OrderStatus.CANCELLED, order.status)
    }

    @Test
    fun `applying admin decision to an order not in review throws`() {
        val order = groceryOrder(ceiling = Money.ofRupees(1000))
        // never reconciled — still CONFIRMED, not PENDING_ADMIN_REVIEW
        assertThrows(IllegalArgumentException::class.java) {
            service.applyAdminDecision(order, AdminVarianceDecision.Approve(UUID.randomUUID()))
        }
    }

    @Test
    fun `capture is idempotent - repeated capture for same order does not double charge`() {
        val orderId = UUID.randomUUID()
        val first = gateway.capture(orderId, Money.ofRupees(500))
        val second = gateway.capture(orderId, Money.ofRupees(500))
        assertEquals(first, second)
    }

    @Test
    fun `Fresh Market order cannot be passed into Grocery Delivery variance reconciliation`() {
        val freshMarketOrder = Order(
            orderType = OrderType.FRESH_MARKET,
            customerId = UUID.randomUUID(),
            colonyId = UUID.randomUUID(),
            items = listOf(OrderItem(productVariantId = UUID.randomUUID(), freeformDescription = null, quantity = 2, unitPrice = Money.ofRupees(100))),
            deliveryFee = Money.ofRupees(50)
        )
        assertThrows(IllegalArgumentException::class.java) {
            service.reconcile(freshMarketOrder, Money.ofRupees(200), "https://receipts/x.jpg", 15)
        }
    }

    @Test
    fun `Money isWithinVariance rejects negative tolerance`() {
        assertThrows(IllegalArgumentException::class.java) {
            Money.ofRupees(100).isWithinVariance(Money.ofRupees(100), -5)
        }
    }
}
