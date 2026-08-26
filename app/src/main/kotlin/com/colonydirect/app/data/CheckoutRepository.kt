package com.colonydirect.app.data

import com.colonydirect.app.network.ApiClient
import com.colonydirect.app.network.CheckoutApi
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.dto.AddressRequest
import com.colonydirect.app.network.dto.AddressResponse
import com.colonydirect.app.network.dto.CheckoutRequest
import com.colonydirect.app.network.dto.CheckoutResponse
import com.colonydirect.app.network.safeApiCall

class CheckoutRepository(private val api: CheckoutApi) {

    suspend fun getAddresses(): NetworkResult<List<AddressResponse>> =
        safeApiCall(ApiClient.gson) { api.getAddresses() }

    suspend fun addAddress(request: AddressRequest): NetworkResult<AddressResponse> =
        safeApiCall(ApiClient.gson) { api.addAddress(request) }

    suspend fun processCheckout(addressId: String, paymentMethod: String): NetworkResult<CheckoutResponse> =
        safeApiCall(ApiClient.gson) {
            api.processCheckout(CheckoutRequest(addressId = addressId, paymentMethod = paymentMethod))
        }
}
