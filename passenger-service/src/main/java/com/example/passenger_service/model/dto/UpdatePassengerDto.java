package com.example.passenger_service.model.dto;

import com.example.passenger_service.model.enums.Gender;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UpdatePassengerDto(
        String name,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$",
                message = "Неверный формат номера телефона!")
        String phoneNumber,

        Gender gender
) {
}