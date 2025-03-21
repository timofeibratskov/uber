package com.example.rating_service.repo

import com.example.rating_service.entity.UserRatingEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRatingRepo:MongoRepository<UserRatingEntity,Long> {
    fun findByRecipientId(recipientId: Long): UserRatingEntity?

}