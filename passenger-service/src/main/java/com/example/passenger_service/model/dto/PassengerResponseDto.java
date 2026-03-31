package com.example.passenger_service.model.dto;

import com.example.passenger_service.model.enums.Gender;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PassengerResponseDto(
        UUID id,
        String name,
        String email,
        String phoneNumber,
        BigDecimal rating,
        Gender gender
) {
}
