package com.example.payment_service.infrastructure.persistence.jdbc;

import com.example.payment_service.infrastructure.persistence.entity.PaymentMethodEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JdbcPaymentMethodRepository
        extends CrudRepository<PaymentMethodEntity, UUID> {

    List<PaymentMethodEntity> findAllByUserId(UUID userId);

    Optional<PaymentMethodEntity> findByUserIdAndIsDefaultTrue(UUID userId);
}
