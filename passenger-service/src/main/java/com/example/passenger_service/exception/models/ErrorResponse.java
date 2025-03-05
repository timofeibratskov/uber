package com.example.passenger_service.exception.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class ErrorResponse {
    private String code;
    private String message;
}
