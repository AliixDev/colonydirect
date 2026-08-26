package com.colonydirect.order

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderItemRepository : JpaRepository<OrderItem, UUID> {
    fun findAllByOrderId(orderId: UUID): List<OrderItem>
}