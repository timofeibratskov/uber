package com.example.ride_service.model.event;

import lombok.Builder;
import org.springframework.data.geo.Point;

import java.util.UUID;

@Builder
public record RideCreateEvent(
        UUID rideId,
        Integer seats,
        Point startPoint
) {
}
