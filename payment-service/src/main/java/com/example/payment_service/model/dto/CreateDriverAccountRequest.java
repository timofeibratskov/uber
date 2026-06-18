package com.example.payment_service.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateDriverAccountRequest(
        @NotNull(message = "driver id is required")
        UUID driverId,

        @NotBlank(message = "email is required")
        @Email(message = "invalid email!")
        String email
) {
}
