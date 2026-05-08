package com.example.rating_service.exception.models

data class ValidationErrorResponse(
    val code: String,
    val message: String,
    val errors: List<ValidationError>,
)
