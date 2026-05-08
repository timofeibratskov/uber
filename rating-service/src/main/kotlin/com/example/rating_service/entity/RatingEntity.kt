package com.example.rating_service.entity

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "rating_table")
data class RatingEntity(
    @Id
    var id: UUID,
    var rideId: UUID,
    var targetUserId: UUID,
    var rating: Int
)
