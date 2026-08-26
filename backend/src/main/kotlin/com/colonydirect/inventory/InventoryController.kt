package com.colonydirect.inventory

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/inventory")
@PreAuthorize("hasRole('ADMIN')")
class InventoryController(
    private val inventoryService: InventoryService
) {

    @PostMapping("/adjust")
    fun adjustStock(
        authentication: Authentication,
        @Valid @RequestBody request: InventoryAdjustmentRequest
    ): ResponseEntity<InventoryTransactionResponse> {
        val adminId = UUID.fromString(authentication.name)
        val response = inventoryService.adjustStock(adminId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/variants/{variantId}/stock")
    fun getStockLevel(@PathVariable variantId: UUID): ResponseEntity<StockLevelResponse> {
        val response = inventoryService.getStockLevel(variantId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/variants/{variantId}/transactions")
    fun getTransactionHistory(
        @PathVariable variantId: UUID,
        @PageableDefault(size = 50) pageable: Pageable
    ): ResponseEntity<Page<InventoryTransactionResponse>> {
        val response = inventoryService.getVariantTransactionHistory(variantId, pageable)
        return ResponseEntity.ok(response)
    }
}