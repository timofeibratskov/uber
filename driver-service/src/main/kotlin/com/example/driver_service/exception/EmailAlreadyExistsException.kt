package com.example.driver_service.exception

import org.springframework.http.HttpStatus

class EmailAlreadyExistsException(email: String) : BaseException(
    HttpStatus.CONFLICT,
    ErrorResponse(
        error = "email.already.exists",
        description = "Пользователь с таким email уже существует: $email"
    )
)
