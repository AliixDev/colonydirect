package com.colonydirect.app.data

import com.colonydirect.app.network.ApiClient
import com.colonydirect.app.network.CatalogApi
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.dto.CategoryResponse
import com.colonydirect.app.network.dto.PagedProductResponse
import com.colonydirect.app.network.dto.ProductDetailResponse
import com.colonydirect.app.network.safeApiCall

class CatalogRepository(private val api: CatalogApi) {

    suspend fun getCategories(): NetworkResult<List<CategoryResponse>> =
        safeApiCall(ApiClient.gson) { api.getCategories() }

    suspend fun searchProducts(
        categoryId: String? = null,
        productType: String? = null,
        query: String? = null,
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<PagedProductResponse> =
        safeApiCall(ApiClient.gson) {
            api.searchProducts(categoryId, productType, query, page, size)
        }

    suspend fun getProductBySlug(slug: String): NetworkResult<ProductDetailResponse> =
        safeApiCall(ApiClient.gson) { api.getProductBySlug(slug) }
}
