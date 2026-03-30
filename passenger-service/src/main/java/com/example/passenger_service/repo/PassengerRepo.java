package com.example.passenger_service.repo;

import com.example.passenger_service.model.entity.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PassengerRepo
        extends JpaRepository<PassengerEntity, UUID> {
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}