package com.example.driver_service.exception

import org.springframework.http.HttpStatus

class NameAlreadyExistsException(email: String) : BaseException(
    HttpStatus.CONFLICT,
    ErrorResponse(
        error = "name.already.exists",
        description = "Пользователь с таким именем уже существует: $email"
    )
)
