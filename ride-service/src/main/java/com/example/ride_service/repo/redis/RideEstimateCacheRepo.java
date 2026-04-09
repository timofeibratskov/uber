package com.example.ride_service.repo.redis;

import com.example.ride_service.model.cache.RideEstimateCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RideEstimateCacheRepo
        extends JpaRepository<RideEstimateCache, UUID> {
}
