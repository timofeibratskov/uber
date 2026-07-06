package com.example.passenger_service.model.events;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String type
) {
}
