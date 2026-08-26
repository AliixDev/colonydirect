package com.colonydirect.delivery

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "rider_profiles")
class RiderProfile(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: UUID,

    @Column(name = "vehicle_type", nullable = false, length = 50)
    var vehicleType: String,

    @Column(name = "license_number", length = 100)
    var licenseNumber: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "is_available", nullable = false)
    var isAvailable: Boolean = false,

    @Column(name = "current_latitude", precision = 10, scale = 8)
    var currentLatitude: BigDecimal? = null,

    @Column(name = "current_longitude", precision = 11, scale = 8)
    var currentLongitude: BigDecimal? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}