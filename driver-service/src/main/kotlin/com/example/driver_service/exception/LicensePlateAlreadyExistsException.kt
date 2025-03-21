package com.example.driver_service.exception

import org.springframework.http.HttpStatus

class LicensePlateAlreadyExistsException(licensePlate: String) : BaseException(
    HttpStatus.CONFLICT,
    ErrorResponse(
        error = "license.plate.already.exists",
        description = "Автомобиль с таким номерным знаком уже существует: $licensePlate"
    )
)
