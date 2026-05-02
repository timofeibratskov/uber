package com.example.payment_service.infrastructure.persistence;

import com.example.payment_service.domain.model.DriverAccount;
import com.example.payment_service.domain.repository.DriverAccountRepository;
import com.example.payment_service.infrastructure.persistence.jdbc.JdbcDriverAccountRepository;
import com.example.payment_service.infrastructure.persistence.mapper.DriverAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DriverAccountRepositoryImpl implements DriverAccountRepository {
    private final JdbcDriverAccountRepository jdbcDriverAccountRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final DriverAccountMapper mapper;

    @Override
    public Optional<DriverAccount> findByDriverId(UUID driverId) {
        return jdbcDriverAccountRepository.findByDriverId(driverId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<DriverAccount> findByAccountId(UUID accountId) {
        return jdbcDriverAccountRepository.findByAccountId(accountId)
                .map(mapper::toDomain);
    }

    @Override
    public DriverAccount insert(DriverAccount account) {
        return mapper.toDomain(jdbcAggregateTemplate.insert(mapper.toEntity(account)));
    }
}
