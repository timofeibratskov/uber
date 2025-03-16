package com.example.rating_service.exception

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MissingRequiredFieldException::class)
    fun handleMissingFieldException(ex: MissingRequiredFieldException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(ex.message ?: "Missing required field"),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(ex.message ?: "Resource not found"),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(InvalidFormatException::class)
    fun handleInvalidFormatException(ex: InvalidFormatException): ResponseEntity<ErrorResponse> {
        val fieldName = ex.path.joinToString(".") { it.fieldName }
        val enumValues = ex.targetType?.enumConstants?.map { it.toString() }
        val errorMessage = "Invalid value for field '$fieldName'"

        return ResponseEntity(
            ErrorResponse(
                message = errorMessage,
                field = fieldName,
                acceptedValues = enumValues
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(ex.message ?: "Internal Server Error"),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}