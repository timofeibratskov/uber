package com.example.driver_service.exception

import org.springframework.http.HttpStatus

class InvalidCredentialsException : BaseException(
    HttpStatus.UNAUTHORIZED,
    ErrorResponse(
        error = "invalid.credentials",
        description = "Неверный пароль или имя пользователя"
    )
)
