package com.example.driver_service.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class CarDto(
    @Schema(description = "ID машины", example = "1")
    val id: Long?,

    @Schema(description = "Номерной знак", example = "А123ВС77")
    @NotBlank(message = "Номерной знак не может быть пустым")
    val licensePlate: String,

    @Schema(description = "Цвет машины", example = "Синий")
    val color: String,

    @Schema(description = "ID водителя", example = "10")
    val driverId: Long,

    @Schema(description = "Марка машины", example = "Toyota")
    val brand: String,

    @Schema(description = "Количество мест", example = "4")
    @Min(value = 1, message = "Максимальное количество мест: 1")
    @Max(value = 8, message = "Максимальное количество мест: 8")
    val seats: Byte
)
