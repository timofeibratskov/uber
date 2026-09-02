package com.example.passenger_service.model.dto;

import com.example.passenger_service.model.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record CompleteProfileRequestDto(
        @NotBlank(message = "Имя пользователя должно быть!")
        String name,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$",
                message = "Неверный формат номера телефона!")
        @NotBlank(message = "Номер телефона должен быть!")
        String phoneNumber,

        @NotNull
        Gender gender
) {
}