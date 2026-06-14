package com.example.ride_service.model.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record RideCompletedEvent(
        UUID rideId,
        UUID driverId,
        UUID passengerId,
        BigDecimal amount
) {
}
