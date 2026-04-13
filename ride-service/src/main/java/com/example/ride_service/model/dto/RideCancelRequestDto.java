package com.example.ride_service.model.dto;

import com.example.ride_service.model.enums.CancelInitiator;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RideCancelRequestDto(
        @NotNull(message = "initiator is required")
        CancelInitiator cancelInitiator,

        String comment
) {
}