package com.example.passenger_service.model.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record FavoriteAddressResponseDto(
        UUID id,
        String label,
        String address,
        Double latitude,
        Double longitude
) {
}