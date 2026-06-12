package com.example.ride_service.model.dto;

import lombok.Builder;

@Builder
public record RideCancelRequestDto(
        String comment
) {
}