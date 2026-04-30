package com.example.driver_service.model.event

import com.example.driver_service.model.enums.CancelInitiator
import java.util.UUID

data class NoDriversEvent(
    val rideId: UUID,
    val reason: String,
    val initiator: CancelInitiator
)
