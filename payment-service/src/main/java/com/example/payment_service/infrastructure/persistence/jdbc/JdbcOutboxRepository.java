package com.example.payment_service.infrastructure.persistence.jdbc;

import com.example.payment_service.infrastructure.persistence.entity.OutboxEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JdbcOutboxRepository
        extends CrudRepository<OutboxEntity, Integer> {
    List<OutboxEntity> findAllByOrderByCreatedAt();
}
