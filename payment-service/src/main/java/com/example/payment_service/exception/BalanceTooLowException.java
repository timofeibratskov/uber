package com.example.payment_service.exception;

public class BalanceTooLowException extends BaseException {
    public BalanceTooLowException(String message) {
        super(message);
    }
}
