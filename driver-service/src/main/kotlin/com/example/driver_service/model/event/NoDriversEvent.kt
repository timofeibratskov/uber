package com.example.driver_service.model.event

import java.util.UUID

data class NoDriversEvent(
    val rideId: UUID,
    val reason: String,
)
