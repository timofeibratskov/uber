package com.example.payment_service.domain.repository;

import com.example.payment_service.infrastructure.persistence.entity.OutboxEntity;

import java.util.List;

public interface OutboxRepository {
    void save(OutboxEntity entity);

    void deleteById(Long id);

    List<OutboxEntity> findAllByOrderByCreatedAt();
}
