package com.example.rating_service.dto

import com.example.rating_service.enums.SenderType

data class RatingIdInRideEvent(
    val rideId: String,
    val recipientRatingId: Long,
    val type: SenderType,
)
