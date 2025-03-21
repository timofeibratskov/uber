package com.example.rating_service.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "user_rating")
data class UserRatingEntity(
    @Id
    var id: Long = 0,
    val recipientId: Long,
    var totalRating: Float = 0.0f,
    var quantityRating: Int = 0
)
