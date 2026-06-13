package com.example.ride_service.model.dto;

import com.example.ride_service.model.enums.PaymentStatus;
import com.example.ride_service.model.enums.RideStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record RideShortResponseDto(
        UUID id,
        String startAddress,
        String stopAddress,
        BigDecimal finalAmount,
        RideStatus status,
        PaymentStatus paymentStatus,
        LocalDateTime createdAt
) {
}
