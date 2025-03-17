package com.example.passenger_service.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginPassengerRequest(

        @Schema(description = "Email водителя", example = "driver@gmail.com")
        @NotBlank(message = "поле почта должно быть заполнено !")
        @Email(message = "почта в неверном формате")
        String gmail,

        @Schema(description = "Пароль водителя", example = "securepassword")
        @NotBlank(message = "поле Пароль должен быть заполнен!")
        String password
) {
}