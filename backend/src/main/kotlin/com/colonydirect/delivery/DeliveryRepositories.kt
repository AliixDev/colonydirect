package com.colonydirect.delivery

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RiderProfileRepository : JpaRepository<RiderProfile, UUID> {
    fun findByUserId(userId: UUID): RiderProfile?
    fun findAllByIsActiveTrueAndIsAvailableTrue(): List<RiderProfile>
}

interface DeliveryRepository : JpaRepository<Delivery, UUID> {
    fun findByOrderId(orderId: UUID): Delivery?
    fun findAllByRiderIdAndStatusIn(riderId: UUID, statuses: List<DeliveryStatus>, pageable: Pageable): Page<Delivery>
    fun findAllByStatus(status: DeliveryStatus, pageable: Pageable): Page<Delivery>
}