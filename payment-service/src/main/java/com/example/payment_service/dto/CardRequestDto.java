package com.example.payment_service.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CardRequestDto(
        @NotNull(message = "cardNumber Должен быть!")
        @Pattern(regexp = "^\\d{4}-\\d{4}-\\d{4}-\\d{4}$", message = "Номер карты должен быть в формате XXXX-XXXX-XXXX-XXXX")
        String cardNumber,
        @NotNull(message = ",баланс должен быть!")
        @Positive(message = "баланс строго положительный")
        BigDecimal balance,
        @NotNull(message = "Пароль должен быть")
        Integer password
) {

}
