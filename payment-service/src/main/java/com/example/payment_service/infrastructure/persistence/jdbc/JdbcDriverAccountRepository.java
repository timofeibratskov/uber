package com.example.payment_service.infrastructure.persistence.jdbc;

import com.example.payment_service.infrastructure.persistence.entity.DriverAccountEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface JdbcDriverAccountRepository
        extends CrudRepository<DriverAccountEntity, UUID> {
    Optional<DriverAccountEntity> findByDriverId(UUID driverId);

    Optional<DriverAccountEntity> findByAccountId(String accountId);
}
