package com.colonydirect.inventory

import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class InventoryAdjustmentRequest(
    @field:NotNull val variantId: UUID,
    @field:NotNull val transactionType: TransactionType,
    @field:NotNull val quantityChange: Int,
    val notes: String? = null
)

data class InventoryTransactionResponse(
    val id: UUID,
    val variantId: UUID,
    val transactionType: TransactionType,
    val quantityChange: Int,
    val referenceId: UUID?,
    val notes: String?,
    val createdAt: Instant
)

data class StockLevelResponse(
    val variantId: UUID,
    val sku: String,
    val variantName: String,
    val currentStock: Int,
    val isLowStock: Boolean
)