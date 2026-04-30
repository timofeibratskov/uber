package com.example.ride_service.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RideCreateRequestDto(
        @NotNull
        UUID passengerId,

        @Min(value = 4, message = "Minimum 4 seats")
        @Max(value = 8, message = "Maximum 8 seats")
        Integer seats) {
}
