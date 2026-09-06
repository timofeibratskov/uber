package com.example.payment_service.service.handler;

public interface PaymentHandler<T> {
    void handle(T event);

    Class<T> getEventType();
}
