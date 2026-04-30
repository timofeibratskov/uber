package com.example.payment_service.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Ride ID is required")
        UUID rideId,

        @Positive(message = "Amount must be positive")
        @NotNull(message = "Amount is required")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotNull(message = "Payment method ID is required")
        UUID paymentMethodId
) {
}
