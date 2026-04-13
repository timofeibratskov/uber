package com.example.ride_service.model.dto;

import lombok.Builder;

@Builder
public record RideAcceptedResponseDto(
        String driverName,
        String carModel,
        String carColor,
        String carBrand,
        String carLicensePlate,
        Integer seats,
        String statusDescription
) {
}