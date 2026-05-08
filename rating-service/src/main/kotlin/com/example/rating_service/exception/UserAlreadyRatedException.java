package com.example.rating_service.exception;

public class UserAlreadyRatedException extends RuntimeException {
    public UserAlreadyRatedException(String message) {
        super(message);
    }
}
