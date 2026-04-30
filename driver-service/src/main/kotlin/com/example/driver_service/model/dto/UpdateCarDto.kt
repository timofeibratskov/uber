package com.example.driver_service.model.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class UpdateCarDto(
    val color: String?,

    val brand: String?,

    val model: String?,

    @field:Min(value = 4, message = "Минимум 4 места")
    @field:Max(value = 8, message = "Максимум 8 мест")
    val seats: Int?
)
