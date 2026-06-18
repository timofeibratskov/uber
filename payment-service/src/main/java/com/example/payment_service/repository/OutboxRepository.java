package com.example.payment_service.repository;

import com.example.payment_service.model.entity.OutboxEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository
        extends CrudRepository<OutboxEntity, Long> {
    List<OutboxEntity> findAllByOrderByCreatedAt();
}
