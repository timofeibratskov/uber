package com.example.payment_service.model.dto;

import lombok.Builder;

@Builder
public record GatewayAuthorizationResult(
        boolean isSuccess,
        String intentId,
        String chargeId,
        String errorMessage
) {
}