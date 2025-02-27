package com.example.payment_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequestDto(

        Long senderId,
        Long recipientId,
       @Positive(message = "amount строго положительный!")
        BigDecimal amount,
        String rideId,
        Integer password
) {
}
