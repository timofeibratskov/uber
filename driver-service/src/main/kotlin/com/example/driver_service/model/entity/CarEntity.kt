package com.example.driver_service.model.entity

import java.time.LocalDateTime
import java.util.UUID


data class CarEntity(
    var id: UUID = UUID.randomUUID(),
    var driverId: UUID? = null,
    var color: String = "",
    var licensePlate: String = "",
    var brand: String = "",
    var model: String = "",
    var seats: Int = 4,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var isDeleted: Boolean = false
)