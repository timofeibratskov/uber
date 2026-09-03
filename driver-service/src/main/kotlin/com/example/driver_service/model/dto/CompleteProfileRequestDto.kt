package com.example.driver_service.model.dto

import com.example.driver_service.model.enums.Gender
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class CompleteProfileRequestDto(
    @field:NotBlank(message = "Имя обязательно")
    val name: String,

    @field:NotBlank(message = "Номер телефона обязателен")
    @field:Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Неверный формат номера телефона")
    val phoneNumber: String,

    @field:NotNull(message = "Пол обязателен")
    val gender: Gender
)