package com.example.ride_service.exception;

import com.example.ride_service.exception.models.ErrorResponse;
import com.example.ride_service.exception.models.ValidationError;
import com.example.ride_service.exception.models.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(NotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                "NOT_FOUND",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        ValidationErrorResponse response = new ValidationErrorResponse(
                "VALIDATION_FAILED",
                "One or more fields are invalid",
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> ValidationError.builder()
                                .field(error.getField())
                                .message(error.getDefaultMessage()).build()
                        )
                        .collect(Collectors.toList())
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EstimateExpiredException.class)
    public ResponseEntity<ErrorResponse> handleEstimateExpired(EstimateExpiredException ex) {
        ErrorResponse error = new ErrorResponse(
                "ESTIMATE_EXPIRED",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR",
                        "Unexpected error occurred")
                );
    }
}
