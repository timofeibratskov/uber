package com.example.payment_service.repository;


import com.example.payment_service.model.entity.PaymentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends
        CrudRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByRideId(UUID rideId);
}
