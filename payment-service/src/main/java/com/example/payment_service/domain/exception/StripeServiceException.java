package com.example.payment_service.domain.exception;

public class StripeServiceException extends RuntimeException {
    public StripeServiceException(String message) {
        super(message);
    }
}
