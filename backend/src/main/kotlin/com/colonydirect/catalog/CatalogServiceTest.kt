package com.colonydirect.catalog

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.util.*

class CatalogServiceTest {
    private val categoryRepository = mock(CategoryRepository::class.java)
    private val productRepository = mock(ProductRepository::class.java)
    private val productVariantRepository = mock(ProductVariantRepository::class.java)
    private val productImageRepository = mock(ProductImageRepository::class.java)

    private val catalogService = CatalogService(
        categoryRepository,
        productRepository,
        productVariantRepository,
        productImageRepository
    )

    @Test
    fun `createCategory creates and returns new category`() {
        val request = CategoryCreateRequest(
            name = "Fresh Vegetables",
            slug = "fresh-vegetables"
        )
        `when`(categoryRepository.existsBySlug("fresh-vegetables")).thenReturn(false)
        `when`(categoryRepository.save(any(Category::class.java))).thenAnswer { it.arguments[0] }

        val response = catalogService.createCategory(request)

        assertNotNull(response)
        assertEquals("Fresh Vegetables", response.name)
        assertEquals("fresh-vegetables", response.slug)
    }
}