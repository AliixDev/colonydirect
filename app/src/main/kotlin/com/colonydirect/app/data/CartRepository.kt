package com.colonydirect.app.data

import com.colonydirect.app.network.ApiClient
import com.colonydirect.app.network.CartApi
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.dto.AddToCartRequest
import com.colonydirect.app.network.dto.CartResponse
import com.colonydirect.app.network.dto.UpdateCartItemRequest
import com.colonydirect.app.network.safeApiCall
import com.colonydirect.app.network.safeApiCallNoBody

class CartRepository(private val api: CartApi) {

    suspend fun getCart(): NetworkResult<CartResponse> =
        safeApiCall(ApiClient.gson) { api.getCart() }

    suspend fun addItem(productId: String, variantId: String?, quantity: Int): NetworkResult<CartResponse> =
        safeApiCall(ApiClient.gson) {
            api.addItem(AddToCartRequest(productId = productId, variantId = variantId, quantity = quantity))
        }

    suspend fun updateItem(itemId: String, quantity: Int): NetworkResult<CartResponse> =
        safeApiCall(ApiClient.gson) {
            api.updateItem(itemId, UpdateCartItemRequest(quantity = quantity))
        }

    suspend fun removeItem(itemId: String): NetworkResult<CartResponse> =
        safeApiCall(ApiClient.gson) { api.removeItem(itemId) }

    suspend fun clearCart(): NetworkResult<Unit> =
        safeApiCallNoBody { api.clearCart() }
}
