package com.colonydirect.inventory

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

enum class TransactionType {
    RESTOCK,
    SALE,
    ADJUSTMENT,
    RETURN,
    SHRINKAGE
}

@Entity
@Table(name = "inventory_transactions")
class InventoryTransaction(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "variant_id", nullable = false)
    val variantId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    val transactionType: TransactionType,

    @Column(name = "quantity_change", nullable = false)
    val quantityChange: Int,

    @Column(name = "reference_id")
    val referenceId: UUID? = null,

    @Column(length = 500)
    val notes: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)