package com.example.payment_service.service.provider;

import com.example.payment_service.model.entity.PaymentEntity;
import com.example.payment_service.model.entity.PaymentMethodEntity;
import com.example.payment_service.model.enums.PaymentType;
import org.springframework.data.geo.Point;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProvider {
    PaymentType getType();

    void authorize(
            PaymentMethodEntity method,
            BigDecimal amount,
            String currency,
            UUID rideId,
            Integer seats,
            Point startPoint);

    void capture(PaymentEntity payment);

    void release(PaymentEntity payment);
}

