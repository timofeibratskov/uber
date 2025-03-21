package com.example.ride_service.exception;


public abstract class RideException extends RuntimeException {
    public RideException(String message) {
        super(message);
    }

}
