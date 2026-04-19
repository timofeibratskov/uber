package com.example.driver_service.model.event

import com.example.driver_service.model.enums.CancelInitiator
import java.time.LocalDateTime
import java.util.UUID

data class RideCancelledEvent(
    val rideId: UUID,
    val driverId: UUID,
    val initiator: CancelInitiator,
    val cancelAt: LocalDateTime
) {
}
