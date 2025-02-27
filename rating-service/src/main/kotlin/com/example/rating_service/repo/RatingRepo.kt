package com.example.rating_service.repo

import com.example.rating_service.entity.RatingEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface RatingRepo:MongoRepository<RatingEntity,Long> {
    fun findByRecipientId(recipientId: Long): List<RatingEntity>

}