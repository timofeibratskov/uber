package com.example.ride_service.repo.db;

import com.example.ride_service.model.entity.RideEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RideRepo
        extends JpaRepository<RideEntity, UUID> {
}
