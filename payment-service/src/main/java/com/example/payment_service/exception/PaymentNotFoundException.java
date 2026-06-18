package com.example.payment_service.exception;

import jakarta.ws.rs.NotFoundException;

public class PaymentNotFoundException extends NotFoundException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
