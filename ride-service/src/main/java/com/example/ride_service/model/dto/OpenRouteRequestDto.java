package com.example.ride_service.model.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record OpenRouteRequestDto(
        List<List<Double>> coordinates
) {
}
