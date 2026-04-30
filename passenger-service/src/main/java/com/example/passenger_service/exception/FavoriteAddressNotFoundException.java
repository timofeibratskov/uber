package com.example.passenger_service.exception;

public class FavoriteAddressNotFoundException extends EntityNotFoundException {
    public FavoriteAddressNotFoundException(String message) {
        super(message);
    }
}
