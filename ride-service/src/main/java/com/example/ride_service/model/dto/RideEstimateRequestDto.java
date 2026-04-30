package com.example.ride_service.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.data.geo.Point;

import java.util.UUID;

@Builder
public record RideEstimateRequestDto(
        @NotNull
        UUID passengerId,

        @NotNull(message = "start point is required")
        Point startPoint,

        @NotNull(message = "stop point is required")
        Point stopPoint,

        @NotNull(message = "start address is required")
        String startAddress,

        @NotNull(message = "stop address is required")
        String stopAddress
) {
}