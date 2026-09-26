package com.example.driver_service.controller.rest

import com.example.driver_service.service.LocationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.data.geo.Point
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Локации", description = "Отправление гео-позиции водителей")
@RestController
@RequestMapping("/api/v1/drivers")
class LocationController (
    private val locationService: LocationService
){

    @Operation(summary = "Обновить гео-позицию водителя (Пинг)", description = "Каждые N секунд отправляет текущие координаты точки Point(x, y)")
    @PostMapping("/{id}/ping")
    fun pingLocation(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID,
        @Valid @RequestBody point: Point
    ): ResponseEntity<Void> {
        locationService.pingLocation(id, point)
        return ResponseEntity.noContent().build()
    }
}