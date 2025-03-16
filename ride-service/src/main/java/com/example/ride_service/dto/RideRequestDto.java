package com.example.ride_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Starting point of the ride", example = "Main St, City")
    private String pointA;

    @NotBlank(message = "Point B is required")
    @Schema(description = "Destination point of the ride", example = "Elm St, City")
    private String pointB;

    @NotNull(message = "Creator ID is required")
    @Schema(description = "ID of the ride creator", example = "1")
    private Long creatorId;

    @Min(value = 1, message = "минимум 1")
    @Max(value = 8, message = "максимум 8")
    @Schema(description = "Number of available seats", example = "4", type = "integer", format = "int32")
    private Byte seats;
}