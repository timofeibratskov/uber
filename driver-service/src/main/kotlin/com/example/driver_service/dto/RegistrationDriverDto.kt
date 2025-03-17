package com.example.driver_service.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class RegistrationDriverDto(

    @Schema(description = "имя", example = "driver1212")
    @NotBlank(message = "имя должно быть!")
    val name: String,

    @Schema(description = "Email водителя", example = "driver@gmail.com")
    @NotBlank(message = "почта должена быть!")
    val gmail: String,

    @NotBlank(message = "Пароль должен быть!")
    @Schema(description = "пароль водителя", example = "superpass")
    val password: String,

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Неверный формат номера телефона!")
    @NotBlank(message = "Номер телефона должен быть!")
    @Schema(description = "телефон водителя", example = "+375295656565")
    val phoneNumber: String
)