package com.colonydirect.catalog

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class CatalogService(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val productVariantRepository: ProductVariantRepository,
    private val productImageRepository: ProductImageRepository
) {
    fun createCategory(request: CategoryCreateRequest): CategoryResponse {
        require(!categoryRepository.existsBySlug(request.slug)) { "Category slug already exists" }
        
        val category = Category(
            parentId = request.parentId,
            name = request.name,
            slug = request.slug,
            description = request.description,
            imageUrl = request.imageUrl,
            displayOrder = request.displayOrder
        )
        return categoryRepository.save(category).toResponse()
    }

    @Transactional(readOnly = true)
    fun getAllActiveCategories(): List<CategoryResponse> {
        return categoryRepository.findAllByActiveTrueOrderByDisplayOrderAsc().map { it.toResponse() }
    }

    fun createProduct(request: ProductCreateRequest): ProductDetailResponse {
        require(categoryRepository.existsById(request.categoryId)) { "Category does not exist" }
        require(productRepository.findBySlug(request.slug) == null) { "Product slug already exists" }

        val product = productRepository.save(
            Product(
                categoryId = request.categoryId,
                name = request.name,
                slug = request.slug,
                description = request.description,
                productType = request.productType
            )
        )

        val variants = request.variants.map { vReq ->
            productVariantRepository.save(
                ProductVariant(
                    productId = product.id,
                    sku = vReq.sku,
                    variantName = vReq.variantName,
                    unit = vReq.unit,
                    unitValue = vReq.unitValue,
                    priceAmount = vReq.priceAmount,
                    discountPriceAmount = vReq.discountPriceAmount,
                    stockQuantity = vReq.stockQuantity
                )
            )
        }

        val images = request.imageUrls.mapIndexed { idx, url ->
            productImageRepository.save(
                ProductImage(
                    productId = product.id,
                    imageUrl = url,
                    isPrimary = idx == 0,
                    displayOrder = idx
                )
            )
        }

        return ProductDetailResponse(
            id = product.id,
            categoryId = product.categoryId,
            name = product.name,
            slug = product.slug,
            description = product.description,
            productType = product.productType,
            active = product.active,
            variants = variants.map { it.toResponse() },
            images = images.map { it.toResponse() }
        )
    }

    @Transactional(readOnly = true)
    fun getProductBySlug(slug: String): ProductDetailResponse {
        val product = productRepository.findBySlug(slug)
            ?: throw NoSuchElementException("Product not found with slug: $slug")
        return buildProductDetailResponse(product)
    }

    @Transactional(readOnly = true)
    fun searchProducts(
        categoryId: UUID?,
        productType: ProductType?,
        query: String?,
        pageable: Pageable
    ): Page<ProductDetailResponse> {
        return productRepository.searchProducts(categoryId, productType, query, pageable)
            .map { buildProductDetailResponse(it) }
    }

    private fun buildProductDetailResponse(product: Product): ProductDetailResponse {
        val variants = productVariantRepository.findAllByProductId(product.id)
        val images = productImageRepository.findAllByProductIdOrderByDisplayOrderAsc(product.id)
        return ProductDetailResponse(
            id = product.id,
            categoryId = product.categoryId,
            name = product.name,
            slug = product.slug,
            description = product.description,
            productType = product.productType,
            active = product.active,
            variants = variants.map { it.toResponse() },
            images = images.map { it.toResponse() }
        )
    }

    private fun Category.toResponse() = CategoryResponse(
        id = id, parentId = parentId, name = name, slug = slug,
        description = description, imageUrl = imageUrl, displayOrder = displayOrder, active = active
    )

    private fun ProductVariant.toResponse() = ProductVariantResponse(
        id = id, sku = sku, variantName = variantName, unit = unit,
        unitValue = unitValue, priceAmount = priceAmount, discountPriceAmount = discountPriceAmount,
        stockQuantity = stockQuantity, active = active
    )

    private fun ProductImage.toResponse() = ProductImageResponse(
        id = id, imageUrl = imageUrl, isPrimary = isPrimary, displayOrder = displayOrder
    )
}