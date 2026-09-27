package com.example.driver_service.model.entity

import com.example.driver_service.model.enums.ShiftStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ShiftEntity(
    val id: UUID = UUID.randomUUID(),
    val driverId: UUID,
    val carId: UUID,
    var status: ShiftStatus = ShiftStatus.OPEN,
    val startAt: LocalDateTime = LocalDateTime.now(),
    var endAt: LocalDateTime? = null,
    var totalRides: Int = 0,
    var totalEarnings: BigDecimal = BigDecimal.ZERO,
    var totalDistanceMeters: Long = 0L
)