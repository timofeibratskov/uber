package com.example.passenger_service.exception;

public class PassengerNotFoundException extends EntityNotFoundException {

    public PassengerNotFoundException(String message) {
        super(message);
    }
}
