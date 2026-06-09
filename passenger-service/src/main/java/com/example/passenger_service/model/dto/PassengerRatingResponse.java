package com.example.passenger_service.model.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PassengerRatingResponse(
        BigDecimal rating
) {
}
