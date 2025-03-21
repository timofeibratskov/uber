package com.example.rating_service.dto

data class UserRatingEvent(
    val recipientId:Long,
    val rating:Float
)
