package com.example.payment_service.infrastructure.web.exception.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ValidationErrorResponse {
    private String code;
    private String message;
    private List<ValidationError> errors;
}
