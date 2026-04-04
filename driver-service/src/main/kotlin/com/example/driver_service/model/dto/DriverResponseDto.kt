package com.example.driver_service.model.dto

import com.example.driver_service.model.enums.Gender
import java.util.UUID

data class DriverResponseDto(
    val id: UUID,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val rating: Float?,
    val gender: Gender,
    val carId: UUID?,
)
