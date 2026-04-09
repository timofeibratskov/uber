package com.example.ride_service.model.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RideEstimateResponseDto(
        Double distanceKm,
        Long durationMin,
        BigDecimal price,
        String polyline
) {
}
