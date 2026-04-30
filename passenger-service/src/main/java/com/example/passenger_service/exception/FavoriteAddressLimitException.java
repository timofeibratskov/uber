package com.example.passenger_service.exception;

public class FavoriteAddressLimitException extends RuntimeException {
    public FavoriteAddressLimitException(String message) {
        super(message);
    }
}
