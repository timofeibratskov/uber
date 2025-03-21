package com.example.driver_service.exception

import org.springframework.http.HttpStatus

abstract class BaseException(
    val httpStatus: HttpStatus,
    val errorResponse: ErrorResponse,

):RuntimeException(errorResponse.description) {
}