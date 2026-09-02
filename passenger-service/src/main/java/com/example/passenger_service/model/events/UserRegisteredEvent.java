package com.example.passenger_service.model.events;


import lombok.Builder;

import java.util.UUID;

@Builder
public record UserRegisteredEvent(
        UUID userId,
        String email,
        String type
) {
}
