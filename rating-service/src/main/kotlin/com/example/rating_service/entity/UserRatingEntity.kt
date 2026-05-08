package com.example.rating_service.entity

import java.math.BigDecimal
import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "user_rating_table")
data class UserRatingEntity(
    @Id
    var id: UUID,
    var userId: UUID,
    var count: Long,
    var totalScore: BigDecimal,
    var rating: BigDecimal
)
