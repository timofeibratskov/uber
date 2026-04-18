package com.example.driver_service.model.event

import java.util.UUID

data class DriverAssignedEvent(
    val rideId: UUID,
    val driverId: UUID,
    val driverName: String,
    val carId: UUID,
    val carModel: String,
    val carColor: String,
    val carBrand: String,
    val carLicensePlate: String,
    val seats: Int
)
