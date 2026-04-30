package com.example.payment_service.infrastructure.persistence.jdbc;

import com.example.payment_service.infrastructure.persistence.entity.PaymentTransactionEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface JdbcPaymentTransactionRepository
        extends CrudRepository<PaymentTransactionEntity, UUID> {
    Optional<PaymentTransactionEntity> findByRideId(UUID rideId);
}
