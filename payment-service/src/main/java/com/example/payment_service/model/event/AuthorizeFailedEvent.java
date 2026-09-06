package com.example.payment_service.model.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AuthorizeFailedEvent(
        UUID rideId,
        String errorMessage
) {
    public static AuthorizeFailedEvent create(UUID rideId, String message) {
        return AuthorizeFailedEvent.builder()
                .rideId(rideId)
                .errorMessage(message)
                .build();
    }
}
