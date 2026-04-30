package com.example.payment_service.infrastructure.web.exception.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ValidationError {
    private String field;
    private String message;
}
