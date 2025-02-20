package com.example.driver_service.exception

import org.springframework.http.HttpStatus

class DriverNotFoundException(id: Long) : BaseException(
    HttpStatus.NOT_FOUND,
    ErrorResponse(
        error = "driver.not.found",
        description = "Такого водителя не существует: $id"
    )
)
