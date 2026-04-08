package com.example.driver_service.model.dto

import com.example.driver_service.model.enums.Gender
import java.math.BigDecimal
import java.util.UUID

data class DriverResponseDto(
    val id: UUID,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val rating: BigDecimal?,
    val gender: Gender,
    val carId: UUID?,
)
