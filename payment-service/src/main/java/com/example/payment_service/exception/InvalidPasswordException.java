package com.example.payment_service.exception;

public class InvalidPasswordException extends BaseException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
