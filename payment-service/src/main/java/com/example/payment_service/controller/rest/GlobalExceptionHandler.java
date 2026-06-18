package com.example.payment_service.controller.rest;

import com.example.payment_service.exception.EntityNotFoundException;
import com.example.payment_service.exception.PaymentDeclinedException;
import com.example.payment_service.exception.PaymentMethodLimitExceededException;
import com.example.payment_service.exception.ResourceAlreadyExistsException;
import com.example.payment_service.exception.StripeServiceException;
import com.example.payment_service.exception.models.ErrorResponse;
import com.example.payment_service.exception.models.ValidationError;
import com.example.payment_service.exception.models.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .code("NOT_FOUND")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .code("ALREADY_EXISTS")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(PaymentMethodLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleLimitExceeded(PaymentMethodLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .code("LIMIT_EXCEEDED")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(StripeServiceException.class)
    public ResponseEntity<ErrorResponse> handleStripeServiceError(StripeServiceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.builder()
                        .code("STRIPE_SERVICE_ERROR")
                        .message("External payment provider is temporarily unavailable")
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .code("VALIDATION_FAILED")
                .message("Input data validation failed")
                .errors(ex.getBindingResult().getFieldErrors().stream()
                        .map(error -> ValidationError.builder()
                                .field(error.getField())
                                .message(error.getDefaultMessage())
                                .build())
                        .toList())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .code("INVALID_STATE")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .code("INTERNAL_SERVER_ERROR")
                        .message("Unexpected error occurred")
                        .build());
    }
}