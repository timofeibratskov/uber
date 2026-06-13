package com.example.ride_service.model.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DriverResponseDto(
        UUID driverId,
        String name,
        String carBrand,
        String carModel,
        String carLicensePlate,
        String carColor) {
}
