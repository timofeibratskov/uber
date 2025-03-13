package com.example.payment_service.dto;

import com.example.payment_service.enums.Role;
import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record CardResponseDto(
        Long id,
        String cardNumber,
        BigDecimal balance,
        Integer password,
        Role role,
        Long ownerId
) {
}
