package com.example.driver_service.dto

data class DriverDto(
    val id: Long?,
    val name: String,
    val gmail: String,
    val phoneNumber: String,
    val password:String,
    val rating: Float
)
