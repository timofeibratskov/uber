package com.example.passenger_service.exception.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ValidationErrorResponse {
    private String code;
    private String message;
    private List<ValidationError> errors;
}
