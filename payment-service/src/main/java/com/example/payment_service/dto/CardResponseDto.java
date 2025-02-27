package com.example.payment_service.dto;

import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record CardResponseDto(
        Long id,
        String cardNumber,
        BigDecimal balance,
        Integer password
) {
}
