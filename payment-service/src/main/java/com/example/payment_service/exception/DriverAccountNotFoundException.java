package com.example.payment_service.exception;

public class DriverAccountNotFoundException extends EntityNotFoundException {
    public DriverAccountNotFoundException(String message) {
        super(message);
    }
}
