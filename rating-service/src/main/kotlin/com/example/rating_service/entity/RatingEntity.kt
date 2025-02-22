package com.example.rating_service.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "rating")
data class RatingEntity(
    @Id
    var id: Long = 0,
    val rating: Float,
    val description: String?,
    val senderId: Long,
    val recipientId: Long,
    val createdAt: Instant = Instant.now()
)
