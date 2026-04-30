package com.example.driver_service.model.entity

import com.example.driver_service.model.enums.Gender
import com.example.driver_service.model.enums.WorkStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID


data class DriverEntity(
    var id: UUID = UUID.randomUUID(),
    var name: String = "",
    var email: String = "",
    var password: String = "",
    var phoneNumber: String = "",
    var rating: BigDecimal? = null,
    var gender: Gender = Gender.OTHER,
    var carId: UUID? = null,
    var workStatus: WorkStatus = WorkStatus.OFF_DUTY,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)