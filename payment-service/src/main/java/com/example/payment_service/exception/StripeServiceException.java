package com.example.payment_service.exception;

public class StripeServiceException extends RuntimeException {
    public StripeServiceException(String message) {
        super(message);
    }
}
