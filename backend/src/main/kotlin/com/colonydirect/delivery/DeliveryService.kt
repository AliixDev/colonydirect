package com.colonydirect.delivery

import com.colonydirect.order.OrderRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class DeliveryService(
    private val riderProfileRepository: RiderProfileRepository,
    private val deliveryRepository: DeliveryRepository,
    private val orderRepository: OrderRepository
) {

    fun createRiderProfile(request: RiderProfileCreateRequest): RiderProfileResponse {
        require(riderProfileRepository.findByUserId(request.userId) == null) {
            "Rider profile already exists for this user"
        }
        val profile = riderProfileRepository.save(
            RiderProfile(
                userId = request.userId,
                vehicleType = request.vehicleType,
                licenseNumber = request.licenseNumber
            )
        )
        return profile.toResponse()
    }

    fun updateRiderLocation(userId: UUID, request: RiderLocationUpdateRequest): RiderProfileResponse {
        val profile = riderProfileRepository.findByUserId(userId)
            ?: throw NoSuchElementException("Rider profile not found")

        profile.currentLatitude = request.latitude
        profile.currentLongitude = request.longitude
        request.isAvailable?.let { profile.isAvailable = it }
        
        return riderProfileRepository.save(profile).toResponse()
    }

    fun getAvailableRiders(): List<RiderProfileResponse> {
        return riderProfileRepository.findAllByIsActiveTrueAndIsAvailableTrue()
            .map { it.toResponse() }
    }

    fun initializeDelivery(orderId: UUID): DeliveryResponse {
        require(orderRepository.existsById(orderId)) { "Order not found" }
        require(deliveryRepository.findByOrderId(orderId) == null) { "Delivery already initialized for order" }

        val delivery = deliveryRepository.save(
            Delivery(orderId = orderId)
        )
        return delivery.toResponse()
    }

    fun assignDelivery(deliveryId: UUID, request: DeliveryAssignmentRequest): DeliveryResponse {
        val delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow { NoSuchElementException("Delivery not found") }
        
        require(riderProfileRepository.existsById(request.riderId)) { "Rider not found" }
        require(delivery.status == DeliveryStatus.PENDING) { "Delivery is not pending" }

        delivery.riderId = request.riderId
        delivery.status = DeliveryStatus.ASSIGNED
        return deliveryRepository.save(delivery).toResponse()
    }

    fun updateDeliveryStatus(userId: UUID, deliveryId: UUID, request: DeliveryStatusUpdateRequest): DeliveryResponse {
        val delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow { NoSuchElementException("Delivery not found") }
            
        val riderProfile = riderProfileRepository.findByUserId(userId)
            ?: throw IllegalStateException("User is not a rider")
            
        require(delivery.riderId == riderProfile.id) { "Rider is not assigned to this delivery" }

        delivery.status = request.status
        request.notes?.let { delivery.deliveryNotes = it }

        if (request.status == DeliveryStatus.PICKED_UP) {
            delivery.pickupTime = Instant.now()
        } else if (request.status == DeliveryStatus.DELIVERED || request.status == DeliveryStatus.FAILED) {
            delivery.deliveryTime = Instant.now()
        }

        return deliveryRepository.save(delivery).toResponse()
    }

    @Transactional(readOnly = true)
    fun getRiderDeliveries(userId: UUID, statuses: List<DeliveryStatus>, pageable: Pageable): Page<DeliveryResponse> {
        val riderProfile = riderProfileRepository.findByUserId(userId)
            ?: throw IllegalStateException("User is not a rider")
            
        return deliveryRepository.findAllByRiderIdAndStatusIn(riderProfile.id, statuses, pageable)
            .map { it.toResponse() }
    }

    private fun RiderProfile.toResponse() = RiderProfileResponse(
        id = id,
        userId = userId,
        vehicleType = vehicleType,
        licenseNumber = licenseNumber,
        isAvailable = isAvailable,
        currentLatitude = currentLatitude,
        currentLongitude = currentLongitude
    )

    private fun Delivery.toResponse() = DeliveryResponse(
        id = id,
        orderId = orderId,
        riderId = riderId,
        status = status,
        pickupTime = pickupTime,
        deliveryTime = deliveryTime,
        deliveryNotes = deliveryNotes
    )
}