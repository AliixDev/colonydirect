package com.colonydirect.checkout

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface UserAddressRepository : JpaRepository<UserAddress, UUID> {
    fun findAllByUserIdOrderByIsDefaultDescCreatedAtDesc(userId: UUID): List<UserAddress>
    fun findByIdAndUserId(id: UUID, userId: UUID): UserAddress?
    
    @Modifying
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.userId = :userId AND a.id != :excludedId")
    fun unsetDefaultForOtherAddresses(userId: UUID, excludedId: UUID)
}