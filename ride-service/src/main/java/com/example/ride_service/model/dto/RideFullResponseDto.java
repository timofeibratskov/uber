package com.example.ride_service.model.dto;

import com.example.ride_service.model.enums.PaymentStatus;
import com.example.ride_service.model.enums.RideStatus;
import lombok.Builder;
import org.springframework.data.geo.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Builder
public record RideFullResponseDto(
        UUID id,
        UUID passengerId,
        DriverResponseDto driver,
        String startAddress,
        String stopAddress,
        Point startPoint,
        Point stopPoint,
        String polyline,
        BigDecimal finalAmount,
        Integer seats,
        RideStatus status,
        PaymentStatus paymentStatus,
        LocalDateTime createdAt,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime cancelAt,
        String reason
) {
}
