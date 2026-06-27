package com.example.driver_service.model.event

import java.time.LocalDateTime
import java.util.UUID

data class RideCanceledEvent(
    val rideId: UUID,
    val driverId: UUID,
    val cancelAt: LocalDateTime
) {
}
