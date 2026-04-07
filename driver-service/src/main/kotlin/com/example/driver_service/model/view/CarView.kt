package com.example.driver_service.model.view

import java.util.UUID

data class CarView(
    var id: UUID = UUID.randomUUID(),
    var color: String? = null,
    var licensePlate: String? = null,
    var brand: String? = null,
    var model: String? = null,
    var seats: Int? = null
)
