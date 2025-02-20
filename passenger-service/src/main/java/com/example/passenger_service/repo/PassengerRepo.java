package com.example.passenger_service.repo;

import com.example.passenger_service.entity.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerRepo extends JpaRepository<PassengerEntity, Long> {
    Optional<PassengerEntity> findPassengerByGmail(String gmail);

    Optional<PassengerEntity> findPassengerByName(String name);

    Optional<PassengerEntity> findPassengerById(Long id);
}