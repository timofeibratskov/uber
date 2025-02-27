package com.example.payment_service.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CardRequestDto(
        @NotNull(message = "cardnumber Должен быть!")
        String cardNumber,
        @NotNull(message = ",баланс должен быть!")
        @Positive(message = "баланс строго положительный")
        BigDecimal balance,
        @NotNull(message = "Пароль должен быть")
        Integer password
) {

}
