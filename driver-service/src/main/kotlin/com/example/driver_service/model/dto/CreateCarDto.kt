package com.example.driver_service.model.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CreateCarDto(
    @field:NotBlank(message = "Цвет должен быть указан")
    val color: String,

    @field:NotBlank(message = "Номер не может быть пустым")
    @field:Pattern(
        regexp = "^([A-Z]{1}\\d{4}[A-Z]{2}-\\d{1}|\\d{4}[A-Z]{2}-\\d{1})$",
        message = "Неверный формат номера. Пример: E3305AM-4 или 3305AM-1"
    )
    val licensePlate: String,

    @field:NotBlank(message = "Марка обязательна")
    val brand: String,

    @field:NotBlank(message = "Модель обязательна")
    val model: String,

    @field:Min(value = 4, message = "Минимум 4 места")
    @field:Max(value = 8, message = "Максимум 8 мест")
    val seats: Int
)