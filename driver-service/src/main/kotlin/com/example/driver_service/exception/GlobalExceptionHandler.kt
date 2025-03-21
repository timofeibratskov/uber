package com.example.driver_service.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(ex: BaseException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ex.errorResponse, ex.httpStatus)
    }

    @ExceptionHandler(DriverNotFoundException::class)
    fun handleDriverNotFoundException(ex: DriverNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ex.errorResponse, ex.httpStatus)
    }

    @ExceptionHandler(CarNotFoundException::class)
    fun handleCarNotFoundException(ex: CarNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ex.errorResponse, ex.httpStatus)
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ex.errorResponse, ex.httpStatus)
    }

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExistsException(ex: EmailAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ex.errorResponse, ex.httpStatus)
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException::class)
    fun handlePhoneNumberAlreadyExistsException(ex: PhoneNumberAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ex.errorResponse, ex.httpStatus)
    }

    @ExceptionHandler(LicensePlateAlreadyExistsException::class)
    fun handleLicensePlateAlreadyExistsException(ex: LicensePlateAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ex.errorResponse, ex.httpStatus)
    }

    @ExceptionHandler(EmailNotFoundException::class)
    fun handleEmailNotFoundException(ex: EmailNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ex.errorResponse, ex.httpStatus)
    }


    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorDetails = ErrorResponse("server.error", ex.message ?: "Unknown error")
        return ResponseEntity(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
