package com.example.payment_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CardRequestDto(
        @Schema(description = "Card number in format XXXX-XXXX-XXXX-XXXX", example = "1234-5678-9012-3456")
        @NotNull(message = "cardNumber Должен быть!")
        @Pattern(regexp = "^\\d{4}-\\d{4}-\\d{4}-\\d{4}$", message = "Номер карты должен быть в формате XXXX-XXXX-XXXX-XXXX")
        String cardNumber,

        @Schema(description = "Initial balance on the card", example = "500.00")
        @NotNull(message = ",баланс должен быть!")
        @Positive(message = "баланс строго положительный")
        BigDecimal balance,

        @Schema(description = "Card password", example = "1234")
        @NotNull(message = "Пароль должен быть")
        Integer password
) {}