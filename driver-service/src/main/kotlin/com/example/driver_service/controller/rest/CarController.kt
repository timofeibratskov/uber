package com.example.driver_service.controller.rest

import com.example.driver_service.model.dto.CarResponseDto
import com.example.driver_service.model.dto.CreateCarDto
import com.example.driver_service.model.dto.UpdateCarDto
import com.example.driver_service.service.CarService
import com.example.driver_service.service.DriverService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Автомобили водителя", description = "Управление автомобилями, привязанными к конкретному водителю")
@RestController
@RequestMapping("/api/v1/drivers/{driverId}/cars")
class CarController(
    private val carService: CarService,
    private val driverService: DriverService
) {

    @Operation(
        summary = "Получить список машин водителя",
        description = "Возвращает все автомобили, привязанные к этому водителю"
    )
    @GetMapping
    fun getCars(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable driverId: UUID
    ): ResponseEntity<List<CarResponseDto>> {
        return ResponseEntity.ok(carService.findAllByDriverId(driverId))
    }

    @Operation(
        summary = "Получить конкретную машину водителя",
        description = "Возвращает детальную информацию по машине"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Машина успешно найдена"),
            ApiResponse(responseCode = "404", description = "Машина или водитель не найдены", content = [Content()])
        ]
    )
    @GetMapping("/{carId}")
    fun getCar(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable driverId: UUID,

        @Parameter(description = "ID машины", example = "987b6543-f21a-43e5-b789-123456789abc")
        @PathVariable carId: UUID
    ): ResponseEntity<CarResponseDto> {
        return ResponseEntity.ok(carService.findByCarIdAndDriverId(carId, driverId))
    }

    @Operation(
        summary = "Привязать новую машину",
        description = "Добавляет новый автомобиль в профиль водителя"
    )
    @PostMapping
    fun linkCar(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable driverId: UUID,

        @Valid @RequestBody dto: CreateCarDto
    ): ResponseEntity<CarResponseDto> {
        return ResponseEntity.ok(driverService.linkCar(driverId, dto))
    }

    @Operation(
        summary = "Обновить параметры машины",
        description = "Изменение номера, цвета или характеристик автомобиля"
    )
    @PatchMapping("/{carId}")
    fun updateCar(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable driverId: UUID,

        @Parameter(description = "ID машины", example = "987b6543-f21a-43e5-b789-123456789abc")
        @PathVariable carId: UUID,

        @Valid @RequestBody dto: UpdateCarDto
    ): ResponseEntity<CarResponseDto> {
        return ResponseEntity.ok(carService.update(driverId, carId, dto))
    }

    @Operation(summary = "Сделать машину основной", description = "Выбирает конкретную машину для поездок на смене")
    @PatchMapping("/{carId}/main")
    fun assignCarAsMain(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable driverId: UUID,

        @Parameter(description = "ID машины", example = "987b6543-f21a-43e5-b789-123456789abc")
        @PathVariable carId: UUID
    ): ResponseEntity<CarResponseDto> {
        return ResponseEntity.ok(driverService.assignCarAsMain(driverId, carId))
    }

    @Operation(summary = "Удалить/отвязать машину", description = "Удаляет автомобиль из профиля водителя")
    @DeleteMapping("/{carId}")
    fun deleteCar(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable driverId: UUID,

        @Parameter(description = "ID машины", example = "987b6543-f21a-43e5-b789-123456789abc")
        @PathVariable carId: UUID
    ): ResponseEntity<Void> {
        driverService.unlinkCar(driverId, carId)
        return ResponseEntity.noContent().build()
    }
}