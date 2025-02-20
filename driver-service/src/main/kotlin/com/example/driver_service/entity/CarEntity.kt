package com.example.driver_service.entity


data class CarEntity(
    var id: Long,
    var driverId: Long,
    var color: String,
    var licensePlate: String,
    var brand: String,
    var seats: Byte
)