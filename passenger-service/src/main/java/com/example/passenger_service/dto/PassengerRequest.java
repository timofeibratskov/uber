package com.example.passenger_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PassengerRequest(
        @Schema(description = "Имя пассажира", example = "Иван Иванов")
        @NotBlank(message = "Имя пользователя обязательно!")
        String name,

        @Schema(description = "Email пассажира", example = "example@gmail.com")
        @NotBlank(message="Почта должна быть заполнена!")
        @Email(message = "Почта в неверном формате!")
        String gmail,

        @Schema(description = "Пароль пассажира", example = "password123")
        @NotBlank(message = "Пароль должен быть!")
        String password,

        @Schema(description = "Номер телефона пассажира", example = "+79111234567")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Неверный формат номера телефона!")
        @NotBlank(message = "Номер телефона должен быть!")
        String phoneNumber
) {}
