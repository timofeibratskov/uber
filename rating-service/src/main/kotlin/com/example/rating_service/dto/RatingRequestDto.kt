package com.example.rating_service.dto

import java.math.BigDecimal
import java.util.UUID


data class RatingRequestDto(
    val rideId: UUID,
    val rating: BigDecimal,
    val targetUserId: UUID,
)