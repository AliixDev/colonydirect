package com.colonydirect.app.network

import com.colonydirect.app.network.dto.CustomerDashboardSummaryResponse
import retrofit2.Response
import retrofit2.http.GET

interface DashboardApi {

    @GET("${ApiConstants.API_VERSION}/customer/dashboard/summary")
    suspend fun getCustomerSummary(): Response<CustomerDashboardSummaryResponse>
}
