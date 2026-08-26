package com.colonydirect.app.network

import com.colonydirect.app.network.dto.AddressRequest
import com.colonydirect.app.network.dto.AddressResponse
import com.colonydirect.app.network.dto.CheckoutRequest
import com.colonydirect.app.network.dto.CheckoutResponse
import retrofit2.Response
import retrofit2.http.*

interface CheckoutApi {

    @GET("${ApiConstants.API_VERSION}/checkout/addresses")
    suspend fun getAddresses(): Response<List<AddressResponse>>

    @POST("${ApiConstants.API_VERSION}/checkout/addresses")
    suspend fun addAddress(@Body request: AddressRequest): Response<AddressResponse>

    @POST("${ApiConstants.API_VERSION}/checkout/process")
    suspend fun processCheckout(@Body request: CheckoutRequest): Response<CheckoutResponse>
}
