package com.example.rating_service.repo

import com.example.rating_service.entity.RatingEntity
import java.util.UUID
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface RatingRepo : MongoRepository<RatingEntity, UUID> {
    fun findByTargetUserId(targetUserId: UUID): List<RatingEntity>

    fun existsByRideId(rideId: UUID): Boolean
}