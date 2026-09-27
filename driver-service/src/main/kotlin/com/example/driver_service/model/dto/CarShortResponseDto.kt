package com.example.driver_service.model.dto

import java.util.UUID

class CarShortResponseDto(
    val id: UUID,
    val licensePlate: String,
    val brand: String,
    val model: String
)