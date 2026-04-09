package com.example.ride_service.model.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record RideCreateResponseDto(
        UUID id,
        String statusDescription,
        BigDecimal price,
        String polyline,
        LocalDateTime createdAt
) {
}
