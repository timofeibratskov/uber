package com.example.ride_service.model.event;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record RideCanceledEvent(
        UUID rideId,
        UUID driverId,
        LocalDateTime cancelAt
) {
}
