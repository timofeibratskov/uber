package com.example.ride_service.repo;

import com.example.ride_service.entity.RideEntity;
import com.example.ride_service.enums.RideStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRepo extends MongoRepository<RideEntity,String> {
    List<RideEntity> findAllByCreatorId(Long id);
    List<RideEntity> findByStatus(RideStatus status);
    List<RideEntity> findAllByDriverId(Long id);
}
