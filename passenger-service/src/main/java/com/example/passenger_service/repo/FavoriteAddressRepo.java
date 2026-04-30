package com.example.passenger_service.repo;

import com.example.passenger_service.model.entity.FavoriteAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FavoriteAddressRepo
        extends JpaRepository<FavoriteAddressEntity, UUID> {
    List<FavoriteAddressEntity> findByPassengerId(UUID passengerId);

    int deleteByIdAndPassengerId(UUID id, UUID passengerId);
}
