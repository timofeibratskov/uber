package com.example.rating_service.repo

import com.example.rating_service.entity.UserRatingEntity
import java.util.UUID
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRatingRepo : MongoRepository<UserRatingEntity, UUID> {
    fun findByUserId(userId: UUID): UserRatingEntity?
}