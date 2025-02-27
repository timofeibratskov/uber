package com.example.ride_service.exception;

public class InvalidStatusException extends RideException {
    public InvalidStatusException(String message) {
        super(message);
    }
}
