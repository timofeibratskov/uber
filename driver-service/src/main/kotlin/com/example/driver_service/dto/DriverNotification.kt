package com.example.driver_service.dto

import java.time.LocalDateTime

data class DriverNotification(
    val id: String,
    val pointA: String,
    val pointB: String,
    val creatorId: Long,
    val seats: Byte,
    val time: LocalDateTime
)
