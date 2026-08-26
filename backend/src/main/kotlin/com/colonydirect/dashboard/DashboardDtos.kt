package com.colonydirect.dashboard

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class AdminOverviewResponse(
    val todayRevenue: BigDecimal,
    val todayOrdersCount: Long,
    val pendingDeliveriesCount: Long,
    val lowStockVariantsCount: Long,
    val activeRidersCount: Long
)

data class DailyRevenueStat(
    val date: String,
    val totalOrders: Int,
    val totalRevenue: BigDecimal
)

data class RevenueAnalyticsResponse(
    val startDate: String,
    val endDate: String,
    val totalRevenue: BigDecimal,
    val totalOrders: Int,
    val dailyBreakdown: List<DailyRevenueStat>
)

data class CustomerDashboardSummaryResponse(
    val totalOrdersPlaced: Long,
    val totalAmountSpent: BigDecimal,
    val activeOrdersCount: Long,
    val defaultAddressColony: String?
)

data class OrderSummaryItem(
    val orderId: UUID,
    val status: String,
    val totalAmount: BigDecimal,
    val itemCount: Int,
    val createdAt: Instant
)