package com.colonydirect.ai

import com.colonydirect.catalog.ProductDetailResponse
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class AiSearchRequest(
    @field:NotBlank val query: String
)

data class AiSearchResponse(
    val query: String,
    val detectedLanguage: String,
    val expandedTerms: List<String>,
    val products: List<ProductDetailResponse>
)

data class OcrTextRequest(
    @field:NotEmpty val rawTextLines: List<String>
)

data class OcrItemMatch(
    val originalText: String,
    val matchedProduct: ProductDetailResponse?,
    val confidenceScore: Double
)

data class OcrParseResponse(
    val items: List<OcrItemMatch>
)

data class SmartReorderItemResponse(
    val product: ProductDetailResponse,
    val defaultVariantId: UUID?,
    val recommendedQuantity: Int,
    val isDueForReorder: Boolean
)