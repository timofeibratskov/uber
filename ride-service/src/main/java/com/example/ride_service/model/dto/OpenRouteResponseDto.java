package com.example.ride_service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenRouteResponseDto(
        List<RouteDto> routes
) {
    public RouteDto firstRoute() {
        if (routes != null && !routes.isEmpty()) return routes.get(0);
        throw new IllegalStateException("Маршруты не найдены в ответе API!");
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RouteDto(
            SummaryDto summary,
            String geometry
    ) {
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SummaryDto(
            double distance,
            double duration
    ) {
    }
}
