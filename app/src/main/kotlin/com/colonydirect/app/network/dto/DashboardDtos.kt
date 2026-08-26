package com.colonydirect.app.network.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

// ─── Dashboard DTOs ───────────────────────────────────────────────────────────

data class CustomerDashboardSummaryResponse(
    @SerializedName("totalOrdersPlaced") val totalOrdersPlaced: Long,
    @SerializedName("totalAmountSpent") val totalAmountSpent: BigDecimal,
    @SerializedName("activeOrdersCount") val activeOrdersCount: Long,
    @SerializedName("defaultAddressColony") val defaultAddressColony: String?
)
