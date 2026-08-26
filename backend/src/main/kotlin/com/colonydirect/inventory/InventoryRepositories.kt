package com.colonydirect.inventory

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InventoryTransactionRepository : JpaRepository<InventoryTransaction, UUID> {
    fun findAllByVariantIdOrderByCreatedAtDesc(variantId: UUID, pageable: Pageable): Page<InventoryTransaction>
    fun findAllByReferenceId(referenceId: UUID): List<InventoryTransaction>
}