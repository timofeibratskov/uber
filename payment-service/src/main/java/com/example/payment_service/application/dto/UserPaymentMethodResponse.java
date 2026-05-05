package com.example.payment_service.application.dto;

import com.example.payment_service.domain.model.PaymentType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserPaymentMethodResponse(
        UUID id,
        PaymentType type
) {
}
