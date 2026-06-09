package com.example.rating_service.repo;

import com.example.rating_service.model.entity.UserRatingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRatingRepo extends MongoRepository<UserRatingEntity, String> {
    Optional<UserRatingEntity> findByTargetUserId(UUID userId);
}
