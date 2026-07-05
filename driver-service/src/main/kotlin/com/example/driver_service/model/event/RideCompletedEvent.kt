package com.example.driver_service.model.event

import java.math.BigDecimal
import java.util.UUID

data class RideCompletedEvent(
    val rideId: UUID,
    val driverId: UUID,
    val passengerId: UUID,
    val amount: BigDecimal,
    val paymentMethodId: UUID,
    val currency: String
)
