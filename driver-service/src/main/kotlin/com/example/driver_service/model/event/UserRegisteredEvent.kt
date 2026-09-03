package com.example.driver_service.model.event

import java.util.UUID

data class UserRegisteredEvent(
    val userId: UUID,
    val email: String,
    val type: String
)
