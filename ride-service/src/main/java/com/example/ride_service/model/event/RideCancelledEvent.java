package com.example.ride_service.model.event;

import com.example.ride_service.model.enums.CancelInitiator;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record RideCancelledEvent(
        UUID rideId,
        UUID driverId,
        CancelInitiator initiator,
        LocalDateTime cancelAt
) {
}
