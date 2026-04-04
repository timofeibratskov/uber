package com.example.driver_service.exception.models

data class ValidationError(
    val field: String,
    val message: String
)