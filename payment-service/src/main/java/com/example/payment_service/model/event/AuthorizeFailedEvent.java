package com.example.payment_service.model.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AuthorizeFailedEvent(
        UUID rideId,
        String errorMessage
) {
    public static AuthorizeFailedEvent create(RideCreatedEvent event, String message) {
        return AuthorizeFailedEvent.builder()
                .rideId(event.rideId())
                .errorMessage(message)
                .build();
    }
}
