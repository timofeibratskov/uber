package com.example.driver_service.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginDriverDto(

    @Schema(description = "Email водителя", example = "driver@gmail.com")
    @NotBlank(message = "потча должна быть заполнена!!!")
    @Email(message = "неверный формат почты!!!")
    val gmail: String,

    @Schema(description = "Пароль водителя", example = "securepassword")
    @NotBlank(message = "пароль должен быть не пустым!!!")
    val password: String
)
