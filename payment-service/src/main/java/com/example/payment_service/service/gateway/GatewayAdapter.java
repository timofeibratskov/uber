package com.example.payment_service.service.gateway;

import com.example.payment_service.model.dto.GatewayOperationResult;

public interface GatewayAdapter {

    String createAccount(String username);

    GatewayOperationResult authorize(
            Long amountInCents,
            String currency,
            String paymentMethodToken
    );

    GatewayOperationResult capture(String intentId);


    GatewayOperationResult release(String intentId);
}
