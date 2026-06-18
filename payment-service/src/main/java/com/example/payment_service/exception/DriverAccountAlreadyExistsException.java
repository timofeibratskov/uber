package com.example.payment_service.exception;

public class DriverAccountAlreadyExistsException extends ResourceAlreadyExistsException {
    public DriverAccountAlreadyExistsException(String message) {
        super(message);
    }
}
