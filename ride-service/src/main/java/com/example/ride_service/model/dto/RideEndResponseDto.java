package com.example.ride_service.model.dto;

import com.example.ride_service.model.enums.RideStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record RideEndResponseDto(
        UUID id,
        RideStatus status,
        BigDecimal finalAmount,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Long durationMinutes
) {
}