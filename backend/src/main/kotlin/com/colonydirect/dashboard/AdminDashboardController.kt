package com.colonydirect.dashboard

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
class AdminDashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/overview")
    fun getOverview(): ResponseEntity<AdminOverviewResponse> {
        val response = dashboardService.getAdminOverview()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/analytics/revenue")
    fun getRevenueAnalytics(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<RevenueAnalyticsResponse> {
        val response = dashboardService.getRevenueAnalytics(startDate, endDate)
        return ResponseEntity.ok(response)
    }
}