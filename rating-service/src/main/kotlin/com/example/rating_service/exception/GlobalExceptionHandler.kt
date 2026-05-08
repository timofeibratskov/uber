package com.example.rating_service.exception


import com.example.rating_service.exception.models.ErrorResponse
import com.example.rating_service.exception.models.ValidationError
import com.example.rating_service.exception.models.ValidationErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyRatedException::class)
    fun handleUserAlreadyRatedException(ex: UserAlreadyRatedException):
            ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("CONFLICT", ex.message!!),
            HttpStatus.CONFLICT
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


    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception): ResponseEntity<ErrorResponse> {
        print("SERVER ERROR: ${ex.message}")
        return ResponseEntity(
            ErrorResponse("SERVER_ERROR", "Unknown error"),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}