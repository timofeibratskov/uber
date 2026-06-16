package com.example.payment_service.application.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record RideCompletedEvent(
        UUID rideId,
        UUID passengerId,
        UUID paymentMethodId,
        UUID driverId,
        BigDecimal amount,
        String currency
) {
}
