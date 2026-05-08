package com.example.rating_service.exception.models

data class ErrorResponse(
    val code: String,
    val message: String
)