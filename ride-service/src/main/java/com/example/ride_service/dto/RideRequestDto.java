package com.example.ride_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor


public class RideRequestDto {
    @NotBlank(message = "Point A is required")
    private String pointA;

    @NotBlank(message = "Point B is required")
    private String pointB;

    @NotNull(message = "Creator ID is required")
    private Long creatorId;

    @Min(value = 1, message = "минимум 1")
    @Max(value = 8, message = "максимум 8")
    private Byte seats;
}