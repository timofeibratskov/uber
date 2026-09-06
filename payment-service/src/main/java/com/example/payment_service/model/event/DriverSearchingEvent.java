package com.example.payment_service.model.event;

import lombok.Builder;
import org.springframework.data.geo.Point;

import java.util.UUID;

@Builder
public record DriverSearchingEvent(
        UUID rideId,
        Integer seats,
        Point startPoint
) {
    public static DriverSearchingEvent create(
            UUID rideId,
            Integer seats,
            Point startPoint) {
        return DriverSearchingEvent.builder()
                .rideId(rideId)
                .seats(seats)
                .startPoint(startPoint)
                .build();
    }
}