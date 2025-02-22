package com.example.rating_service.dto

data class RatingRequestDto(
    val rating: Float,
    val description: String?,
    val senderId: Long,
    val recipientId: Long
)