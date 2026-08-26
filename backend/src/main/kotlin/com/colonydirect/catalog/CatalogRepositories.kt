package com.colonydirect.catalog

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CategoryRepository : JpaRepository<Category, UUID> {
    fun findBySlug(slug: String): Category?
    fun findAllByParentIdOrderByDisplayOrderAsc(parentId: UUID?): List<Category>
    fun findAllByActiveTrueOrderByDisplayOrderAsc(): List<Category>
    boolean existsBySlug(slug: String)
}

interface ProductRepository : JpaRepository<Product, UUID> {
    fun findBySlug(slug: String): Product?
    fun findAllByCategoryId(categoryId: UUID, pageable: Pageable): Page<Product>
    
    @Query("""
        SELECT p FROM Product p 
        WHERE p.active = true 
        AND (:categoryId IS NULL OR p.categoryId = :categoryId)
        AND (:productType IS NULL OR p.productType = :productType)
        AND (:searchQuery IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) 
             OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchQuery, '%')))
    """)
    fun searchProducts(
        @Param("categoryId") categoryId: UUID?,
        @Param("productType") productType: ProductType?,
        @Param("searchQuery") searchQuery: String?,
        pageable: Pageable
    ): Page<Product>
}

interface ProductVariantRepository : JpaRepository<ProductVariant, UUID> {
    fun findAllByProductId(productId: UUID): List<ProductVariant>
    fun findBySku(sku: String): ProductVariant?
}

interface ProductImageRepository : JpaRepository<ProductImage, UUID> {
    fun findAllByProductIdOrderByDisplayOrderAsc(productId: UUID): List<ProductImage>
    fun findAllByVariantId(variantId: UUID): List<ProductImage>
}