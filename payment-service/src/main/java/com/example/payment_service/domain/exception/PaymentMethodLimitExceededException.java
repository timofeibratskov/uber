package com.example.payment_service.domain.exception;

public class PaymentMethodLimitExceededException extends RuntimeException {
    public PaymentMethodLimitExceededException(String message) {
        super(message);
    }
}
