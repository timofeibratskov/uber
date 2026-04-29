package com.example.payment_service.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentRequest(
        UUID userId,
        UUID rideId,
        BigDecimal amount,
        String currency,
        UUID paymentMethodId) {
}
