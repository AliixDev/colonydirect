package com.colonydirect.app.network

import com.colonydirect.app.network.dto.CategoryResponse
import com.colonydirect.app.network.dto.PagedProductResponse
import com.colonydirect.app.network.dto.ProductDetailResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CatalogApi {

    @GET("${ApiConstants.API_VERSION}/catalog/categories")
    suspend fun getCategories(): Response<List<CategoryResponse>>

    @GET("${ApiConstants.API_VERSION}/catalog/products")
    suspend fun searchProducts(
        @Query("categoryId") categoryId: String? = null,
        @Query("productType") productType: String? = null,
        @Query("q") query: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PagedProductResponse>

    @GET("${ApiConstants.API_VERSION}/catalog/products/{slug}")
    suspend fun getProductBySlug(@Path("slug") slug: String): Response<ProductDetailResponse>
}
