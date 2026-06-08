package com.example.rating_service.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record RatingRequestDto(
        @NotNull(message = "ID оценивающего не может быть пустым")
        UUID raterUserId,

        @NotNull(message = "ID цели не может быть пустым")
        UUID targetUserId,

        @NotNull(message = "ID поездки обязателен")
        UUID rideId,

        @NotNull(message = "Рейтинг обязателен")
        @Digits(integer = 1, fraction = 2, message = "Рейтинг должен иметь максимум 1 знак до запятой и 2 знака после")
        @DecimalMin(value = "0.0", message = "Минимальный рейтинг — 0.0")
        @DecimalMax(value = "5.0", message = "Максимальный рейтинг — 5.0")
        Integer rating
) {
}
