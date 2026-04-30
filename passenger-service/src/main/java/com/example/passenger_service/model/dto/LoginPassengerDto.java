package com.example.passenger_service.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginPassengerDto(
        @NotBlank(message = "Почта должна быть!")
        @Email(message = "Почта в неверном формате!")
        String email,

        @NotBlank(message = "Пароль должен быть!")
        String password
) {
}
