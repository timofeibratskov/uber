package com.example.payment_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequestDto(
        @Schema(description = "ID of the sender", example = "1")
        Long senderId,

        @Schema(description = "ID of the recipient", example = "2")
        Long recipientId,

        @Schema(description = "Amount to transfer", example = "100.00")
        @Positive(message = "amount строго положительный!")
        BigDecimal amount,

        @Schema(description = "ID of the ride (optional)", example = "65a1f2b3c4d5e6f7g8h9i0j")
        String rideId,

        @Schema(description = "Card password", example = "1234")
        Integer password
) {}