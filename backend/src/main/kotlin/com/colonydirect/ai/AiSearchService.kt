package com.colonydirect.ai

import com.colonydirect.catalog.CatalogService
import com.colonydirect.catalog.ProductDetailResponse
import com.colonydirect.catalog.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class AiSearchService(
    private val searchLogRepository: SearchLogRepository,
    private val userProductFrequencyRepository: UserProductFrequencyRepository,
    private val productRepository: ProductRepository,
    private val catalogService: CatalogService
) {

    private val urduEnglishMap = mapOf(
        "aloo" to listOf("potato", "aloo"),
        "tamatar" to listOf("tomato", "tamatar"),
        "pyaz" to listOf("onion", "pyaz"),
        "doodh" to listOf("milk", "doodh"),
        "murgi" to listOf("chicken", "murgi"),
        "daal" to listOf("lentil", "daal"),
        "chawal" to listOf("rice", "chawal"),
        "aata" to listOf("flour", "wheat", "aata"),
        "makhani" to listOf("butter", "makhani"),
        "tel" to listOf("oil", "ghee", "tel"),
        "آلو" to listOf("potato", "aloo"),
        "ٹماٹر" to listOf("tomato", "tamatar"),
        "پیاز" to listOf("onion", "pyaz"),
        "دودھ" to listOf("milk", "doodh")
    )

    fun performAiSearch(userId: UUID?, query: String): AiSearchResponse {
        val normalized = query.trim().lowercase()
        val detectedLang = if (normalized.contains(Regex("[\\u0600-\\u06FF]"))) "UR_PK" else "EN"

        val expandedTerms = mutableSetOf(normalized)
        urduEnglishMap.entries.forEach { (key, synonyms) ->
            if (normalized.contains(key)) {
                expandedTerms.addAll(synonyms)
            }
        }

        val pageable = PageRequest.of(0, 20)
        val matchedProducts = mutableListOf<ProductDetailResponse>()

        for (term in expandedTerms) {
            val searchResults = catalogService.searchProducts(null, null, term, pageable)
            matchedProducts.addAll(searchResults.content)
        }

        val distinctProducts = matchedProducts.distinctBy { it.id }

        searchLogRepository.save(
            SearchLog(
                userId = userId,
                rawQuery = query,
                normalizedQuery = normalized,
                detectedLanguage = detectedLang,
                resultCount = distinctProducts.size
            )
        )

        return AiSearchResponse(
            query = query,
            detectedLanguage = detectedLang,
            expandedTerms = expandedTerms.toList(),
            products = distinctProducts
        )
    }

    fun parseOcrShoppingList(request: OcrTextRequest): OcrParseResponse {
        val matches = request.rawTextLines.map { line ->
            val cleanLine = line.trim()
            if (cleanLine.isBlank()) {
                OcrItemMatch(originalText = line, matchedProduct = null, confidenceScore = 0.0)
            } else {
                val searchResult = performAiSearch(null, cleanLine)
                val bestMatch = searchResult.products.firstOrNull()
                val score = if (bestMatch != null) 0.85 else 0.0
                OcrItemMatch(
                    originalText = cleanLine,
                    matchedProduct = bestMatch,
                    confidenceScore = score
                )
            }
        }
        return OcrParseResponse(items = matches)
    }

    @Transactional(readOnly = true)
    fun getSmartReorders(userId: UUID): List<SmartReorderItemResponse> {
        val frequencies = userProductFrequencyRepository.findAllByUserIdOrderByPurchaseCountDesc(userId)
        val now = Instant.now()

        return frequencies.mapNotNull { freq ->
            val product = runCatching {
                catalogService.getProductBySlug(
                    productRepository.findById(freq.productId).orElse(null)?.slug ?: ""
                )
            }.getOrNull() ?: return@mapNotNull null

            val daysSinceLastPurchase = Duration.between(freq.lastPurchasedAt, now).toDays()
            val isDue = daysSinceLastPurchase >= freq.averageIntervalDays

            SmartReorderItemResponse(
                product = product,
                defaultVariantId = freq.variantId ?: product.variants.firstOrNull()?.id,
                recommendedQuantity = 1,
                isDueForReorder = isDue
            )
        }
    }
}