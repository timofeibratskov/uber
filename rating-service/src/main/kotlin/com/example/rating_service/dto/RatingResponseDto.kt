package com.example.rating_service.dto

import java.time.Instant

data class RatingResponseDto (
    val id: Long,
    val rating: Float,
    val description: String?,
    val senderId: Long,
    val recipientId: Long,
    val createdAt: Instant
)