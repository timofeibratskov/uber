package com.example.payment_service.application.dto;

import com.example.payment_service.domain.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreatePaymentMethodRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Payment type is required")
        PaymentType paymentType,

        String externalToken
) {
}