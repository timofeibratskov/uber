package com.example.driver_service.model.view

import com.example.driver_service.model.enums.Gender
import java.util.UUID

data class DriverView(
    var id: UUID = UUID.randomUUID(),
    var name: String? = null,
    var email: String? = null,
    var phoneNumber: String? = null,
    var rating: Float? = null,
    var gender: Gender? = Gender.OTHER,
    var car: CarView? = null
)
