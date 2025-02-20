package com.example.passenger_service.repo;

import com.example.passenger_service.entity.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerRepo extends JpaRepository<PassengerEntity,Long>
{
}
