package com.example.driver_service.model.event

import java.util.UUID

data class UserCreatedEvent(
    val userId: UUID,
    val type: String
)
