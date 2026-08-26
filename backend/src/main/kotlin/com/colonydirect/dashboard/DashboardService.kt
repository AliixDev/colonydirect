package com.colonydirect.dashboard

import com.colonydirect.catalog.ProductVariantRepository
import com.colonydirect.delivery.DeliveryRepository
import com.colonydirect.delivery.DeliveryStatus
import com.colonydirect.delivery.RiderProfileRepository
import com.colonydirect.inventory.InventoryService
import com.colonydirect.order.OrderRepository
import com.colonydirect.checkout.UserAddressRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional(readOnly = true)
class DashboardService(
    private val dailyMetricsSnapshotRepository: DailyMetricsSnapshotRepository,
    private val orderRepository: OrderRepository,
    private val deliveryRepository: DeliveryRepository,
    private val riderProfileRepository: RiderProfileRepository,
    private val productVariantRepository: ProductVariantRepository,
    private val addressRepository: UserAddressRepository
) {

    fun getAdminOverview(): AdminOverviewResponse {
        val allOrders = orderRepository.findAll()
        val today = LocalDate.now()
        
        val todayOrders = allOrders.filter { 
            // Derive date matching logic based on order createdAt
            it.createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate() == today 
        }

        val todayRevenue = todayOrders
            .filter { it.status != "CANCELED" }
            .fold(BigDecimal.ZERO) { acc, order -> acc.add(order.totalAmount) }

        val pendingDeliveries = deliveryRepository.findAllByStatus(
            DeliveryStatus.PENDING,
            org.springframework.data.domain.PageRequest.of(0, 1)
        ).totalElements

        val lowStockCount = productVariantRepository.findAll()
            .count { it.stockQuantity <= InventoryService.LOW_STOCK_THRESHOLD }
            .toLong()

        val activeRidersCount = riderProfileRepository.findAllByIsActiveTrueAndIsAvailableTrue().size.toLong()

        return AdminOverviewResponse(
            todayRevenue = todayRevenue,
            todayOrdersCount = todayOrders.size.toLong(),
            pendingDeliveriesCount = pendingDeliveries,
            lowStockVariantsCount = lowStockCount,
            activeRidersCount = activeRidersCount
        )
    }

    fun getRevenueAnalytics(startDate: LocalDate, endDate: LocalDate): RevenueAnalyticsResponse {
        val snapshots = dailyMetricsSnapshotRepository.findAllBySnapshotDateBetweenOrderBySnapshotDateAsc(startDate, endDate)
        
        val dailyStats = snapshots.map {
            DailyRevenueStat(
                date = it.snapshotDate.toString(),
                totalOrders = it.totalOrders,
                totalRevenue = it.totalRevenue
            )
        }

        val grandTotalRevenue = dailyStats.fold(BigDecimal.ZERO) { acc, stat -> acc.add(stat.totalRevenue) }
        val grandTotalOrders = dailyStats.sumOf { it.totalOrders }

        return RevenueAnalyticsResponse(
            startDate = startDate.toString(),
            endDate = endDate.toString(),
            totalRevenue = grandTotalRevenue,
            totalOrders = grandTotalOrders,
            dailyBreakdown = dailyStats
        )
    }

    fun getCustomerSummary(userId: UUID): CustomerDashboardSummaryResponse {
        val userOrders = orderRepository.findAllByUserIdOrderByCreatedAtDesc(
            userId, 
            org.springframework.data.domain.PageRequest.of(0, 100)
        ).content

        val totalSpent = userOrders
            .filter { it.status != "CANCELED" }
            .fold(BigDecimal.ZERO) { acc, order -> acc.add(order.totalAmount) }

        val activeOrders = userOrders.count { it.status == "PENDING" || it.status == "CONFIRMED" || it.status == "IN_TRANSIT" }.toLong()

        val addresses = addressRepository.findAllByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
        val defaultColony = addresses.firstOrNull()?.colony

        return CustomerDashboardSummaryResponse(
            totalOrdersPlaced = userOrders.size.toLong(),
            totalAmountSpent = totalSpent,
            activeOrdersCount = activeOrders,
            defaultAddressColony = defaultColony
        )
    }
}