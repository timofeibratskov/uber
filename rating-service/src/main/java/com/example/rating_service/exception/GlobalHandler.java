package com.example.rating_service.exception;

import com.example.rating_service.exception.models.ErrorResponse;
import com.example.rating_service.exception.models.ValidationError;
import com.example.rating_service.exception.models.ValidationErrorResponse;
import org.springframework.dao.DuplicateKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
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

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(DuplicateKeyException ex) {
        ErrorResponse error = new ErrorResponse(
                "CONFLICT",
                "Вы уже оставили отзыв к этой поездке."
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "Unexpected error occurred"));
    }
}