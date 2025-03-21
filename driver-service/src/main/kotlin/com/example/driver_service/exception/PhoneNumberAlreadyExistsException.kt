package com.example.driver_service.exception

import org.springframework.http.HttpStatus

class PhoneNumberAlreadyExistsException(phoneNumber: String) : BaseException(
    HttpStatus.CONFLICT,
    ErrorResponse(
        error = "phone.number.already.exists",
        description = "Пользователь с таким номером телефона уже существует: $phoneNumber"
    )
)
