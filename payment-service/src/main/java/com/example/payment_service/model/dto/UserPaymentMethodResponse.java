package com.example.payment_service.model.dto;

import com.example.payment_service.model.enums.PaymentType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserPaymentMethodResponse(
        UUID id,
        PaymentType type
) {
}
