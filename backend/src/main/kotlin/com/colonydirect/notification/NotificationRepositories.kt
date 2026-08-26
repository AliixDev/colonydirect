package com.colonydirect.notification

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DeviceTokenRepository : JpaRepository<DeviceToken, UUID> {
    fun findByToken(token: String): DeviceToken?
    fun findAllByUserId(userId: UUID): List<DeviceToken>
    fun deleteByToken(token: String)
}

interface NotificationRepository : JpaRepository<Notification, UUID> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<Notification>
    fun countByUserIdAndIsReadFalse(userId: UUID): Long
}