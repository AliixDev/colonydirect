package com.colonydirect.auth

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

enum class UserRole { CUSTOMER, RIDER, ADMIN, SUPER_ADMIN }

/**
 * JPA entity for app_user. Business invariants (must have >=1 identifier, etc.)
 * are enforced in AuthService at creation time, not here -- entities must remain
 * constructible by Hibernate/kotlin-jpa's synthetic no-arg constructor.
 */
@Entity
@Table(name = "app_user")
class User(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true)
    var email: String? = null,

    @Column(unique = true)
    var phone: String? = null,

    @Column(name = "password_hash")
    var passwordHash: String? = null,

    @Column(name = "google_sub", unique = true)
    var googleSub: String? = null,

    @Column(name = "full_name", nullable = false)
    var fullName: String = "",

    @Column(name = "preferred_language", nullable = false)
    var preferredLanguage: String = "en",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.CUSTOMER,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
)
