package com.colonydirect.inventory

import com.colonydirect.catalog.ProductVariantRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class InventoryService(
    private val inventoryTransactionRepository: InventoryTransactionRepository,
    private val productVariantRepository: ProductVariantRepository
) {
    companion object {
        const val LOW_STOCK_THRESHOLD = 10
    }

    fun adjustStock(adminId: UUID, request: InventoryAdjustmentRequest): InventoryTransactionResponse {
        val variant = productVariantRepository.findById(request.variantId)
            .orElseThrow { NoSuchElementException("Product variant not found") }

        // Prevent negative stock from adjustments unless explicitly zeroing out
        if (variant.stockQuantity + request.quantityChange < 0) {
            throw IllegalStateException("Adjustment would result in negative stock. Current stock: ${variant.stockQuantity}")
        }

        // Apply stock modification
        variant.stockQuantity += request.quantityChange
        productVariantRepository.save(variant)

        // Record transaction ledger
        val transaction = inventoryTransactionRepository.save(
            InventoryTransaction(
                variantId = variant.id,
                transactionType = request.transactionType,
                quantityChange = request.quantityChange,
                referenceId = adminId,
                notes = request.notes ?: "Admin manual adjustment"
            )
        )

        return transaction.toResponse()
    }

    fun recordSale(orderId: UUID, variantId: UUID, quantitySold: Int): InventoryTransactionResponse {
        val variant = productVariantRepository.findById(variantId)
            .orElseThrow { NoSuchElementException("Product variant not found") }

        if (variant.stockQuantity < quantitySold) {
            throw IllegalStateException("Insufficient stock for variant SKU: ${variant.sku}")
        }

        variant.stockQuantity -= quantitySold
        productVariantRepository.save(variant)

        val transaction = inventoryTransactionRepository.save(
            InventoryTransaction(
                variantId = variant.id,
                transactionType = TransactionType.SALE,
                quantityChange = -quantitySold,
                referenceId = orderId,
                notes = "Order fulfillment"
            )
        )

        return transaction.toResponse()
    }

    @Transactional(readOnly = true)
    fun getStockLevel(variantId: UUID): StockLevelResponse {
        val variant = productVariantRepository.findById(variantId)
            .orElseThrow { NoSuchElementException("Product variant not found") }

        return StockLevelResponse(
            variantId = variant.id,
            sku = variant.sku,
            variantName = variant.variantName,
            currentStock = variant.stockQuantity,
            isLowStock = variant.stockQuantity <= LOW_STOCK_THRESHOLD
        )
    }

    @Transactional(readOnly = true)
    fun getVariantTransactionHistory(variantId: UUID, pageable: Pageable): Page<InventoryTransactionResponse> {
        return inventoryTransactionRepository.findAllByVariantIdOrderByCreatedAtDesc(variantId, pageable)
            .map { it.toResponse() }
    }

    private fun InventoryTransaction.toResponse() = InventoryTransactionResponse(
        id = id,
        variantId = variantId,
        transactionType = transactionType,
        quantityChange = quantityChange,
        referenceId = referenceId,
        notes = notes,
        createdAt = createdAt
    )
}