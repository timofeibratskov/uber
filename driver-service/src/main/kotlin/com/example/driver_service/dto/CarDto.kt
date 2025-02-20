package com.example.driver_service.dto

data class CarDto(
    val id: Long?,
    val driverId: Long,
    val color: String,
    val licensePlate: String,
    val brand: String,
    val seats: Byte
)
