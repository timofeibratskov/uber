package com.example.payment_service.model.event;

import lombok.Builder;
import org.springframework.data.geo.Point;

import java.util.UUID;

@Builder
public record PaymentAuthorizedEvent(
        UUID rideId,
        Integer seats,
        Point startPoint
) {
    public static PaymentAuthorizedEvent create(RideCreatedEvent event) {
        return PaymentAuthorizedEvent.builder()
                .rideId(event.rideId())
                .seats(event.seats())
                .startPoint(event.startPoint())
                .build();
    }
}