package com.example.payment_service.exception;


public class CardNumberAlreadyExistsException extends BaseException {
    public CardNumberAlreadyExistsException(String message) {
        super(message);
    }
}
