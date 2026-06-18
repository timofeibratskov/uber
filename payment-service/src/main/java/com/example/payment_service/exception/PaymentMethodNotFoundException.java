package com.example.payment_service.exception;

public class PaymentMethodNotFoundException extends EntityNotFoundException {
    public PaymentMethodNotFoundException(String message) {
        super(message);
    }
}
