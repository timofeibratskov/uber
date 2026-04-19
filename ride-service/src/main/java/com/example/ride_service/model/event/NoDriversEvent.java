package com.example.ride_service.model.event;

import com.example.ride_service.model.enums.CancelInitiator;
import lombok.Builder;

import java.util.UUID;

@Builder
public record NoDriversEvent(
        UUID rideId,
        String reason,
        CancelInitiator initiator
) {
}