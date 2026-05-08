package com.example.rating_service.dto

import java.util.UUID

data class RatingResponseDto(
    val message: String,
    val targetUserId: UUID
)