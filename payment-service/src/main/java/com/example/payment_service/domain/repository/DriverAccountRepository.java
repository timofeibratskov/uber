package com.example.payment_service.domain.repository;

import com.example.payment_service.domain.model.DriverAccount;

import java.util.Optional;
import java.util.UUID;

public interface DriverAccountRepository {
    Optional<DriverAccount> findByDriverId(UUID driverId);

    Optional<DriverAccount> findByAccountId(String accountId);

    DriverAccount insert(DriverAccount account);
}
