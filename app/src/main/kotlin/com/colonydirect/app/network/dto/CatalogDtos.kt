package com.colonydirect.app.network.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.UUID

// ─── Catalog DTOs (mirrors backend CatalogDtos.kt) ───────────────────────────

data class CategoryResponse(
    @SerializedName("id") val id: String,
    @SerializedName("parentId") val parentId: String?,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("description") val description: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("displayOrder") val displayOrder: Int,
    @SerializedName("active") val active: Boolean
)

data class ProductVariantResponse(
    @SerializedName("id") val id: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("variantName") val variantName: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("unitValue") val unitValue: BigDecimal,
    @SerializedName("priceAmount") val priceAmount: BigDecimal,
    @SerializedName("discountPriceAmount") val discountPriceAmount: BigDecimal?,
    @SerializedName("stockQuantity") val stockQuantity: Int,
    @SerializedName("active") val active: Boolean
)

data class ProductImageResponse(
    @SerializedName("id") val id: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("isPrimary") val isPrimary: Boolean,
    @SerializedName("displayOrder") val displayOrder: Int
)

data class ProductDetailResponse(
    @SerializedName("id") val id: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("description") val description: String?,
    @SerializedName("productType") val productType: String,
    @SerializedName("active") val active: Boolean,
    @SerializedName("variants") val variants: List<ProductVariantResponse>,
    @SerializedName("images") val images: List<ProductImageResponse>
)

data class PagedProductResponse(
    @SerializedName("content") val content: List<ProductDetailResponse>,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("number") val number: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("last") val last: Boolean
)
