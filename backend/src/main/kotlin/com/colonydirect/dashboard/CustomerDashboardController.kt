package com.colonydirect.dashboard

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/customer/dashboard")
class CustomerDashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/summary")
    fun getSummary(authentication: Authentication): ResponseEntity<CustomerDashboardSummaryResponse> {
        val userId = UUID.fromString(authentication.name)
        val response = dashboardService.getCustomerSummary(userId)
        return ResponseEntity.ok(response)
    }
}