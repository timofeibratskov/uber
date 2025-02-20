package com.example.driver_service.exception

import org.springframework.http.HttpStatus

class CarNotFoundException(id: Long) : BaseException(
    HttpStatus.NOT_FOUND,
    ErrorResponse(
        error = "driver.not.found",
        description = "Такого авто не существует: $id"
    )
)
