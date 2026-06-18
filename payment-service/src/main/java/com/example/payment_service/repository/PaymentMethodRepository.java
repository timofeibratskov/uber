package com.example.payment_service.repository;

import com.example.payment_service.model.entity.PaymentMethodEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository
        extends CrudRepository<PaymentMethodEntity, UUID> {

    List<PaymentMethodEntity> findAllByUserIdAndIsDeletedFalse(UUID userId);

    List<PaymentMethodEntity> findAllByUserId(UUID userId);
}