package com.example.driver_service.model.view

import com.example.driver_service.model.enums.Gender
import com.example.driver_service.model.event.DriverAssignedEvent
import java.util.UUID

data class DriverView(
    var id: UUID = UUID.randomUUID(),
    var name: String? = null,
    var email: String? = null,
    var phoneNumber: String? = null,
    var gender: Gender? = Gender.OTHER,
    var car: CarView? = null
)

fun DriverView.toAssignedDriverEvent(rideId: UUID): DriverAssignedEvent {
    val car = this.car ?: throw IllegalStateException("Driver $id has no car assigned")

    return DriverAssignedEvent(
        rideId = rideId,
        driverId = this.id,
        driverName = this.name ?: "Unknown",
        carId = car.id,
        carModel = car.model ?: "",
        carColor = car.color ?: "",
        carBrand = car.brand ?: "",
        carLicensePlate = car.licensePlate ?: "",
        seats = car.seats ?: 4
    )
}
