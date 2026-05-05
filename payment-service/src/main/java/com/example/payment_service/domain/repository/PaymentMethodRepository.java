package com.example.payment_service.domain.repository;

import com.example.payment_service.domain.model.PaymentMethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodRepository {
    Optional<PaymentMethod> findById(UUID id);

    List<PaymentMethod> findAllByUserId(UUID userId);

    List<PaymentMethod> findAllByUserIdAndIsNotDeleted(UUID userId);

    void update(PaymentMethod paymentMethod);

    PaymentMethod insert(PaymentMethod paymentMethod);
}