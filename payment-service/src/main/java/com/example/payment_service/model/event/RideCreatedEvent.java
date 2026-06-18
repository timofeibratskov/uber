package com.example.payment_service.model.event;

import lombok.Builder;
import org.springframework.data.geo.Point;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record RideCreatedEvent(
        UUID rideId,
        UUID passengerId,
        Integer seats,
        Point startPoint,
        UUID paymentMethodId,
        BigDecimal amount,
        String currency
) {
}