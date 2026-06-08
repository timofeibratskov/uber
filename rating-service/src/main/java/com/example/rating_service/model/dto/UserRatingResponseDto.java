package com.example.rating_service.model.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record UserRatingResponseDto(
        BigDecimal rating
) {
}
