package com.example.payment_service.repository;

import com.example.payment_service.model.entity.PaymentTransactionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository
        extends CrudRepository<PaymentTransactionEntity, UUID> {
    Optional<PaymentTransactionEntity> findByPaymentId(UUID paymentId);
}
