package com.example.driver_service.model.dto

import com.example.driver_service.model.enums.ShiftStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ShiftDto(
    val id: UUID,
    val driverId: UUID,
    val carDto: CarShortResponseDto,
    val status: ShiftStatus,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime?,
    val totalRides: Int,
    val totalEarnings: BigDecimal,
    val totalDistanceMeters: Long
)