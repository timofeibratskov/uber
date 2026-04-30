package com.example.payment_service.domain.exception;

public class PaymentMethodAlreadyExistsException extends ResourceAlreadyExistsException {
    public PaymentMethodAlreadyExistsException(String message) {
        super(message);
    }
}
