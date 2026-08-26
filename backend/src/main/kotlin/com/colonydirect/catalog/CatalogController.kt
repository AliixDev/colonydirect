package com.colonydirect.catalog

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/catalog")
class CatalogController(
    private val catalogService: CatalogService
) {
    @GetMapping("/categories")
    fun getCategories(): List<CategoryResponse> {
        return catalogService.getAllActiveCategories()
    }

    @GetMapping("/products")
    fun searchProducts(
        @RequestParam(required = false) categoryId: UUID?,
        @RequestParam(required = false) productType: ProductType?,
        @RequestParam(required = false) q: String?,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<ProductDetailResponse> {
        return catalogService.searchProducts(categoryId, productType, q, pageable)
    }

    @GetMapping("/products/{slug}")
    fun getProductBySlug(@PathVariable slug: String): ProductDetailResponse {
        return catalogService.getProductBySlug(slug)
    }

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    fun createCategory(@Valid @RequestBody request: CategoryCreateRequest): CategoryResponse {
        return catalogService.createCategory(request)
    }

    @PostMapping("/admin/products")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    fun createProduct(@Valid @RequestBody request: ProductCreateRequest): ProductDetailResponse {
        return catalogService.createProduct(request)
    }
}