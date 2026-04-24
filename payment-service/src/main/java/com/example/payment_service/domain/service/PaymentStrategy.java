package com.example.payment_service.domain.service;

import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentTransaction;
import com.example.payment_service.domain.model.PaymentType;

public interface PaymentStrategy {
    void execute(PaymentTransaction transaction, PaymentMethod method);

    PaymentType getType();
}