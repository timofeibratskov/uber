package com.example.payment_service.model.dto;

import lombok.Builder;

@Builder
public record GatewayOperationResult(
        boolean isSuccess,
        String intentId,
        String chargeId,
        String errorMessage
) {
}