package com.example.payment_service.infrastructure.persistence;

import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.repository.PaymentMethodRepository;
import com.example.payment_service.infrastructure.persistence.entity.PaymentMethodEntity;
import com.example.payment_service.infrastructure.persistence.jdbc.JdbcPaymentMethodRepository;
import com.example.payment_service.infrastructure.persistence.mapper.PaymentMethodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentMethodRepositoryImpl implements PaymentMethodRepository {
    private final PaymentMethodMapper mapper;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final JdbcPaymentMethodRepository jdbcPaymentMethodRepository;

    @Override
    public Optional<PaymentMethod> findById(UUID id) {
        var entity = jdbcAggregateTemplate.findById(id, PaymentMethodEntity.class);
        return Optional.of(entity).map(mapper::toDomain);
    }

    @Override
    public List<PaymentMethod> findAllByUserId(UUID userId) {
        return jdbcPaymentMethodRepository.findAllByUserId(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<PaymentMethod> findDefaultByUserId(UUID userId) {
        return jdbcPaymentMethodRepository.findByUserIdAndIsDefaultTrue(userId)
                .map(mapper::toDomain);
    }

    @Override
    public void update(PaymentMethod paymentMethod) {
        jdbcAggregateTemplate.update(mapper.toEntity(paymentMethod));
    }

    @Override
    public PaymentMethod insert(PaymentMethod paymentMethod) {
        return mapper.toDomain(jdbcAggregateTemplate.insert(mapper.toEntity(paymentMethod)));
    }
}