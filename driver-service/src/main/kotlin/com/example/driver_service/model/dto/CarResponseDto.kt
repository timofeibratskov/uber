package com.example.driver_service.model.dto

import java.util.UUID

data class CarResponseDto(
    val id: UUID,
    val color: String,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val seats: Int,
    val driverId: UUID,
)