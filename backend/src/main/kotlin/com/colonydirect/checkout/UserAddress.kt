package com.colonydirect.checkout

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_addresses")
class UserAddress(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "full_name", nullable = false, length = 100)
    var fullName: String,

    @Column(name = "phone_number", nullable = false, length = 20)
    var phoneNumber: String,

    @Column(nullable = false, length = 100)
    var colony: String,

    @Column(length = 50)
    var block: String? = null,

    @Column(length = 100)
    var street: String? = null,

    @Column(name = "house_number", nullable = false, length = 50)
    var houseNumber: String,

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    var deliveryInstructions: String? = null,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,

    @Column(precision = 10, scale = 8)
    var latitude: BigDecimal? = null,

    @Column(precision = 11, scale = 8)
    var longitude: BigDecimal? = null,

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