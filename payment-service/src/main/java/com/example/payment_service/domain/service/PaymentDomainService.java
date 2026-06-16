package com.example.payment_service.domain.service;

import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentTransaction;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PaymentDomainService {
    private final List<PaymentStrategy> strategies;

    public void process(PaymentTransaction transaction, PaymentMethod method) {
        if (!transaction.getPassengerId().equals(method.getUserId())) {
            throw new IllegalStateException("Payment method does not belong to the user");
        }

        transaction.startProcessing();

        PaymentStrategy strategy = strategies.stream()
                .filter(s -> s.getType() == method.getType())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("No strategy found for: " + method.getType()
                        )
                );

        try {
            strategy.execute(transaction, method);

            transaction.complete();

        } catch (Exception e) {
            transaction.fail();
            throw e;
        }
    }
}