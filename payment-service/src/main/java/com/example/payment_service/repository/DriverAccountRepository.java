package com.example.payment_service.repository;

import com.example.payment_service.model.entity.DriverAccountEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverAccountRepository
        extends CrudRepository<DriverAccountEntity, UUID> {
    Optional<DriverAccountEntity> findByDriverId(UUID driverId);

    Optional<DriverAccountEntity> findByAccountId(String accountId);
}
