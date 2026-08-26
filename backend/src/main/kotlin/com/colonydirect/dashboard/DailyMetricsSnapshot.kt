package com.colonydirect.dashboard

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "daily_metrics_snapshots")
class DailyMetricsSnapshot(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "snapshot_date", nullable = false, unique = true)
    val snapshotDate: LocalDate,

    @Column(name = "total_orders", nullable = false)
    var totalOrders: Int = 0,

    @Column(name = "total_revenue", nullable = false, precision = 12, scale = 2)
    var totalRevenue: BigDecimal = BigDecimal.ZERO,

    @Column(name = "active_customers", nullable = false)
    var activeCustomers: Int = 0,

    @Column(name = "new_customers", nullable = false)
    var newCustomers: Int = 0,

    @Column(name = "canceled_orders", nullable = false)
    var canceledOrders: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)