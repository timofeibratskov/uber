package com.example.passenger_service.dto;


import jakarta.validation.constraints.NotBlank;

public record LoginPassengerRequest(
        @NotBlank(message = "поле почта должно быть заполнено !")
        String gmail,
        @NotBlank(message = "поле Пароль должен быть заполнен!")
        String password
) {


}