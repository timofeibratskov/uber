package com.example.driver_service.dto

data class DriverRatingEvent(
    val recipientId: Long,
    val rating: Float
)
