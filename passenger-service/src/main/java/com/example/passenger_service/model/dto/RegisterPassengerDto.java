package com.example.passenger_service.model.dto;

import com.example.passenger_service.model.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterPassengerDto(
        @NotBlank(message = "Имя пользователя должно быть!")
        String name,

        @NotBlank(message = "Почта должна быть!")
        @Email(message = "Почта в неверном формате!")
        String email,

        @NotBlank(message = "Пароль должен быть!")
        @Size(min = 8, max = 16)
        String password,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$",
                message = "Неверный формат номера телефона!")
        @NotBlank(message = "Номер телефона должен быть!")
        String phoneNumber,

        @NotNull
        Gender gender
) {
}