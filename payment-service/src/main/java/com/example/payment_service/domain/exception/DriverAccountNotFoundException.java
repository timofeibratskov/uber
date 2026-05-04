package com.example.payment_service.domain.exception;

public class DriverAccountNotFoundException extends EntityNotFoundException {
    public DriverAccountNotFoundException(String message) {
        super(message);
    }
}
