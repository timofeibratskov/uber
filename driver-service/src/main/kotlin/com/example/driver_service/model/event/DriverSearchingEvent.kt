package com.example.driver_service.model.event

import java.util.UUID
import org.springframework.data.geo.Point

data class DriverSearchingEvent(
    val rideId: UUID,
    val seats: Int,
    val startPoint: Point,
)
