package com.example.payment_service.infrastructure.payment;

import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentTransaction;
import com.example.payment_service.domain.model.PaymentType;
import com.example.payment_service.domain.service.PaymentStrategy;
import org.springframework.stereotype.Component;

@Component
public class CashPaymentStrategy implements PaymentStrategy {
    @Override
    public void execute(PaymentTransaction transaction, PaymentMethod method) {
        System.out.println(">>> [CASH STRATEGY] Payment by cash accepted by ride id: " + transaction.getRideId());
    }

    @Override
    public PaymentType getType() {
        return PaymentType.CASH;
    }
}