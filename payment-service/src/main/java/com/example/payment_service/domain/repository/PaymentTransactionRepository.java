package com.example.payment_service.domain.repository;

import com.example.payment_service.domain.model.PaymentTransaction;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository {
    void save(PaymentTransaction paymentTransaction);

    Optional<PaymentTransaction> findById(UUID id);

    Optional<PaymentTransaction> findByRideId(UUID userId);
}