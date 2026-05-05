package com.example.payment_service.infrastructure.persistence;

import com.example.payment_service.domain.repository.OutboxRepository;
import com.example.payment_service.infrastructure.persistence.entity.OutboxEntity;
import com.example.payment_service.infrastructure.persistence.jdbc.JdbcOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {
    private final JdbcOutboxRepository outboxRepository;

    @Override
    public void save(OutboxEntity entity) {
        outboxRepository.save(entity);
    }

    public void deleteAll() {
        outboxRepository.deleteAll();
    }

    @Override
    public void delete(OutboxEntity entity) {
        outboxRepository.delete(entity);
    }

    @Override
    public List<OutboxEntity> findAllByOrderByCreatedAt() {
        return outboxRepository.findAllByOrderByCreatedAt();
    }
}
