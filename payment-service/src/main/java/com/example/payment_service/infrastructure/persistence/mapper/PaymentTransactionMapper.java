package com.example.payment_service.infrastructure.persistence.mapper;

import com.example.payment_service.domain.model.Money;
import com.example.payment_service.domain.model.PaymentTransaction;
import com.example.payment_service.infrastructure.persistence.entity.PaymentTransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionMapper {
    public PaymentTransactionEntity toEntity(PaymentTransaction domain) {
        return PaymentTransactionEntity.builder()
                .id(domain.getId())
                .rideId(domain.getRideId())
                .passengerId(domain.getPassengerId())
                .driverId(domain.getDriverId())
                .amount(domain.getAmount().amount())
                .currency(domain.getAmount().currency().getCurrencyCode())
                .status(domain.getStatus())
                .build();
    }

    public PaymentTransaction toDomain(PaymentTransactionEntity entity) {
        return PaymentTransaction.restore(
                entity.getId(),
                entity.getRideId(),
                entity.getPassengerId(),
                entity.getDriverId(),
                Money.of(entity.getAmount(), entity.getCurrency()),
                entity.getStatus()
        );
    }
}