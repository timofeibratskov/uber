package com.example.rating_service.dto


data class UserRatingSummaryDto(
    val recipientId: Long,
    val totalRating: Float,
    val quantityRating: Int,
)