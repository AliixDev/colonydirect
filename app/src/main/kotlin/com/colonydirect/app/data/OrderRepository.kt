package com.colonydirect.app.data

import com.colonydirect.app.network.ApiClient
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.OrderApi
import com.colonydirect.app.network.dto.OrderDetailResponse
import com.colonydirect.app.network.dto.PagedOrderResponse
import com.colonydirect.app.network.safeApiCall

class OrderRepository(private val api: OrderApi) {

    suspend fun getOrders(page: Int = 0, size: Int = 20): NetworkResult<PagedOrderResponse> =
        safeApiCall(ApiClient.gson) { api.getOrders(page, size) }

    suspend fun getOrderById(id: String): NetworkResult<OrderDetailResponse> =
        safeApiCall(ApiClient.gson) { api.getOrderById(id) }

    suspend fun cancelOrder(id: String): NetworkResult<OrderDetailResponse> =
        safeApiCall(ApiClient.gson) { api.cancelOrder(id) }
}
