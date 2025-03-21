package com.example.passenger_service.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PassengerRequest(
        @NotBlank(message = "Имя пользователя обязательно!")
        String name,
        @NotBlank(message="почта должна быть заполнена!")
        @Email(message = "Почта в неверном формате!")
        String gmail,
        @NotBlank(message = "пароль должен быть!")
        String password,
        @Pattern(regexp = "^\\+?[0-9]{10,15}$",message = "Неверный формат номера телефона!")
        @NotBlank(message = "номер телефона должен быть!")
        String phoneNumber) {
}
