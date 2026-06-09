package com.example.rating_service.repo;

import com.example.rating_service.model.entity.RatingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepo extends MongoRepository<RatingEntity, String> {
    Optional<RatingEntity> findByTargetUserId(UUID targetUserId);
}
