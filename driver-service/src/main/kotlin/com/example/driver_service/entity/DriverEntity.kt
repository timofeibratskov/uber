package com.example.driver_service.entity


data class DriverEntity(
    var id: Long,
    var name: String,
    var gmail: String,
    var password: String,
    var phoneNumber: String,
    var rating: Float
)