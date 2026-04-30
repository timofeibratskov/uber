package com.example.driver_service.model.dto

import com.example.driver_service.model.enums.Gender
import jakarta.validation.constraints.Pattern

data class UpdateDriverDto(
    val name: String?,

    @field:Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Неверный формат номера телефона!")
    val phoneNumber: String?,

    val gender: Gender?,
)
