package com.colonydirect.app.network

import com.colonydirect.app.network.dto.AddToCartRequest
import com.colonydirect.app.network.dto.CartResponse
import com.colonydirect.app.network.dto.UpdateCartItemRequest
import retrofit2.Response
import retrofit2.http.*

interface CartApi {

    @GET("${ApiConstants.API_VERSION}/cart")
    suspend fun getCart(): Response<CartResponse>

    @POST("${ApiConstants.API_VERSION}/cart/items")
    suspend fun addItem(@Body request: AddToCartRequest): Response<CartResponse>

    @PUT("${ApiConstants.API_VERSION}/cart/items/{itemId}")
    suspend fun updateItem(
        @Path("itemId") itemId: String,
        @Body request: UpdateCartItemRequest
    ): Response<CartResponse>

    @DELETE("${ApiConstants.API_VERSION}/cart/items/{itemId}")
    suspend fun removeItem(@Path("itemId") itemId: String): Response<CartResponse>

    @DELETE("${ApiConstants.API_VERSION}/cart")
    suspend fun clearCart(): Response<Unit>
}
