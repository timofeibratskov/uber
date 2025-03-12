package com.example.rating_service.dto

import com.example.rating_service.enums.SenderType


data class RatingRequestDto(
    val rideId: String,
    val rating: Float,
    val description: String?,
    val senderId: Long,
    val recipientId: Long,
    val senderType: SenderType
)