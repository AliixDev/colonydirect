package com.colonydirect.app.data

import com.colonydirect.app.network.ApiClient
import com.colonydirect.app.network.DashboardApi
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.dto.CustomerDashboardSummaryResponse
import com.colonydirect.app.network.safeApiCall

class DashboardRepository(private val api: DashboardApi) {

    suspend fun getCustomerSummary(): NetworkResult<CustomerDashboardSummaryResponse> =
        safeApiCall(ApiClient.gson) { api.getCustomerSummary() }
}
