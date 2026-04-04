package com.example.driver_service.model.dto

import com.example.driver_service.model.enums.Gender
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class RegisterDriverDto(
    @field:NotBlank(message = "Имя обязательно")
    val name: String,

    @field:NotBlank(message = "Пароль обязателен")
    val password: String,

    @field:NotBlank(message = "Email обязателен")
    @field:Email(message = "Неверный формат email")
    val email: String,

    @field:NotBlank(message = "Номер телефона обязателен")
    @field:Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Неверный формат номера телефона")
    val phoneNumber: String,

    @field:NotNull(message = "Пол обязателен")
    val gender: Gender
)