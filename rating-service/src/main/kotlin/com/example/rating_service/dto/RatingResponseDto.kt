package com.example.rating_service.dto

import com.example.rating_service.enums.SenderType
import java.time.Instant

data class RatingResponseDto (
    val id: Long,
    val rideId:String,
    val rating: Float,
    val description: String?,
    val senderId: Long,
    val senderType: SenderType,
    val recipientId: Long,
    val createdAt: Instant
)