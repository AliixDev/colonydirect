package com.colonydirect.ai

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SearchLogRepository : JpaRepository<SearchLog, UUID>

interface UserProductFrequencyRepository : JpaRepository<UserProductFrequency, UUID> {
    fun findAllByUserIdOrderByPurchaseCountDesc(userId: UUID): List<UserProductFrequency>
    fun findByUserIdAndProductIdAndVariantId(userId: UUID, productId: UUID, variantId: UUID?): UserProductFrequency?
}