package com.example.ride_service.repo.db;

import com.example.ride_service.model.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepo extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findAllByOrderByCreatedAt();
}
