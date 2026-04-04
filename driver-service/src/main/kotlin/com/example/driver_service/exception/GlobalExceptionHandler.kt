package com.example.driver_service.exception

import com.example.driver_service.exception.models.ErrorResponse
import com.example.driver_service.exception.models.ValidationError
import com.example.driver_service.exception.models.ValidationErrorResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: EntityNotFoundException):
            ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("NOT_FOUND", ex.message!!),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(ex: InvalidCredentialsException):
            ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("UNAUTHORIZED", ex.message!!),
            HttpStatus.UNAUTHORIZED
        )
    }

    @ExceptionHandler(CarLimitExceededException::class)
    fun handleCarLimitExceededException(ex: CarLimitExceededException):
            ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("CAR_LIMIT_EXCEEDED", ex.message!!),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException):
            ResponseEntity<ValidationErrorResponse> {
        return ResponseEntity(
            ValidationErrorResponse(
                "VALIDATION_FAILED",
                "One or more fields are invalid",
                ex.bindingResult
                    .fieldErrors
                    .stream()
                    .map { error
                        ->
                        ValidationError(error.field, error.defaultMessage!!)
                    }
                    .toList()
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(ResourceAlreadyExistsException::class)
    fun handleResourceAlreadyExistsException(ex: ResourceAlreadyExistsException):
            ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("CONFLICT", ex.message!!),
            HttpStatus.CONFLICT
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error { "SERVER ERROR: ${ex.message}" }
        return ResponseEntity(
            ErrorResponse("SERVER_ERROR", "Unknown error"),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}