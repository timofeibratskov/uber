package com.example.driver_service.exception

import org.springframework.http.HttpStatus

class EmailNotFoundException(email: String) : BaseException(
    HttpStatus.NOT_FOUND,
    ErrorResponse(
        error = "email.not.found",
        description = "Пользователя с такой почтой не существует: $email"
    )
)
