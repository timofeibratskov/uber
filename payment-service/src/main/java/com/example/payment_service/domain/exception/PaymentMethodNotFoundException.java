package com.example.payment_service.domain.exception;

public class PaymentMethodNotFoundException extends EntityNotFoundException {
    public PaymentMethodNotFoundException(String message) {
        super(message);
    }
}
