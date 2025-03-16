package com.example.rating_service.exception

data class ErrorResponse(
    val message: String,
    val field: String? = null,
    val acceptedValues: List<String>? = null
)