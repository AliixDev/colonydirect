package com.colonydirect.app.network

import com.colonydirect.app.network.dto.OrderDetailResponse
import com.colonydirect.app.network.dto.PagedOrderResponse
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {

    @GET("${ApiConstants.API_VERSION}/orders")
    suspend fun getOrders(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PagedOrderResponse>

    @GET("${ApiConstants.API_VERSION}/orders/{id}")
    suspend fun getOrderById(@Path("id") id: String): Response<OrderDetailResponse>

    @POST("${ApiConstants.API_VERSION}/orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): Response<OrderDetailResponse>
}
