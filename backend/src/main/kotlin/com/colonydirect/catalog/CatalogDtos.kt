package com.colonydirect.catalog

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.UUID

data class CategoryCreateRequest(
    val parentId: UUID? = null,
    @field:NotBlank val name: String,
    @field:NotBlank val slug: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val displayOrder: Int = 0
)

data class CategoryResponse(
    val id: UUID,
    val parentId: UUID?,
    val name: String,
    val slug: String,
    val description: String?,
    val imageUrl: String?,
    val displayOrder: Int,
    val active: Boolean
)

data class ProductCreateRequest(
    @field:NotNull val categoryId: UUID,
    @field:NotBlank val name: String,
    @field:NotBlank val slug: String,
    val description: String? = null,
    val productType: ProductType = ProductType.FRESH_MARKET,
    val variants: List<ProductVariantCreateRequest> = emptyList(),
    val imageUrls: List<String> = emptyList()
)

data class ProductVariantCreateRequest(
    @field:NotBlank val sku: String,
    @field:NotBlank val variantName: String,
    @field:NotNull val unit: MeasurementUnit,
    @field:NotNull val unitValue: BigDecimal,
    @field:NotNull val priceAmount: BigDecimal,
    val discountPriceAmount: BigDecimal? = null,
    @field:Min(0) val stockQuantity: Int = 0
)

data class ProductVariantResponse(
    val id: UUID,
    val sku: String,
    val variantName: String,
    val unit: MeasurementUnit,
    val unitValue: BigDecimal,
    val priceAmount: BigDecimal,
    val discountPriceAmount: BigDecimal?,
    val stockQuantity: Int,
    val active: Boolean
)

data class ProductImageResponse(
    val id: UUID,
    val imageUrl: String,
    val isPrimary: Boolean,
    val displayOrder: Int
)

data class ProductDetailResponse(
    val id: UUID,
    val categoryId: UUID,
    val name: String,
    val slug: String,
    val description: String?,
    val productType: ProductType,
    val active: Boolean,
    val variants: List<ProductVariantResponse>,
    val images: List<ProductImageResponse>
)