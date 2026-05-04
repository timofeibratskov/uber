package com.example.payment_service.infrastructure.persistence;

import com.example.payment_service.domain.model.PaymentTransaction;
import com.example.payment_service.domain.repository.PaymentTransactionRepository;
import com.example.payment_service.infrastructure.persistence.entity.PaymentTransactionEntity;
import com.example.payment_service.infrastructure.persistence.jdbc.JdbcPaymentTransactionRepository;
import com.example.payment_service.infrastructure.persistence.mapper.PaymentTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class PaymentTransactionRepositoryImpl implements PaymentTransactionRepository {
    private final PaymentTransactionMapper mapper;
    private final JdbcPaymentTransactionRepository jdbcPaymentTransactionRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentTransaction insert(PaymentTransaction paymentTransaction) {
        return mapper.toDomain(jdbcAggregateTemplate.insert(mapper.toEntity(paymentTransaction)));
    }

    @Override
    public void update(PaymentTransaction paymentTransaction) {
        jdbcAggregateTemplate.update(mapper.toEntity(paymentTransaction));
    }

    @Override
    public Optional<PaymentTransaction> findById(UUID id) {
        return jdbcPaymentTransactionRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<PaymentTransaction> findByRideId(UUID rideId) {
        return jdbcPaymentTransactionRepository.findByRideId(rideId).map(mapper::toDomain);
    }

    public void deleteAll() {
        jdbcAggregateTemplate.deleteAll(PaymentTransactionEntity.class);
    }
}