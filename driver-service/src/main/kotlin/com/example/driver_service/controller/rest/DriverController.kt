package com.example.driver_service.controller.rest

import com.example.driver_service.model.dto.CarResponseDto
import com.example.driver_service.model.dto.CreateCarDto
import com.example.driver_service.model.dto.DriverResponseDto
import com.example.driver_service.model.dto.CompleteProfileRequestDto
import com.example.driver_service.model.dto.UpdateCarDto
import com.example.driver_service.model.dto.UpdateDriverDto
import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.service.CarService
import com.example.driver_service.service.DriverService
import com.example.driver_service.service.LocationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.data.geo.Point
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Водители", description = "Управление профилями водителей, их автомобилями и рабочим статусом на линии")
@RestController
@RequestMapping("/api/v1/drivers")
class DriverController(
    private val driverService: DriverService,
    private val carService: CarService,
    private val locationService: LocationService
) {
    @Operation(summary = "Регистрация / заполнение профиля", description = "Первичное заполнение личных данных водителя")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Профиль успешно заполнен"),
        ApiResponse(responseCode = "400", description = "Некорректные данные профиля", content = [Content()])
    ])
    @PostMapping("/{id}/profile")
    fun register(
        @Valid @RequestBody dto: CompleteProfileRequestDto,
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID
    ): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(driverService.completeProfile(id, dto))
    }

    @Operation(summary = "Получить профиль водителя", description = "Возвращает информацию о водителе по его ID")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Профиль успешно получен"),
        ApiResponse(responseCode = "404", description = "Водитель не найден", content = [Content()])
    ])
    @GetMapping("/{id}")
    fun findById(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID
    ): ResponseEntity<DriverResponseDto> {
        return ResponseEntity.ok(driverService.findById(id))
    }

    @Operation(summary = "Обновить личные данные водителя", description = "Изменение имени, телефона или других полей водителя")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Профиль успешно обновлен"),
        ApiResponse(responseCode = "400", description = "Ошибка валидации", content = [Content()])
    ])
    @PatchMapping("/{id}")
    fun update(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID,
        @Valid @RequestBody dto: UpdateDriverDto
    ): ResponseEntity<Void> {
        driverService.update(id, dto)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Привязать автомобиль к водителю", description = "Добавляет новый автомобиль в гараж водителя")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Автомобиль успешно привязан")
    ])
    @PostMapping("/{id}/cars")
    fun linkCar(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID,
        @Valid @RequestBody dto: CreateCarDto
    ): ResponseEntity<CarResponseDto> {
        return ResponseEntity.ok(driverService.linkCar(id, dto))
    }

    @Operation(summary = "Получить список машин водителя", description = "Возвращает все автомобили, привязанные к этому водителю")
    @GetMapping("/{id}/cars")
    fun getCars(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID
    ): ResponseEntity<List<CarResponseDto>> {
        return ResponseEntity.ok(carService.findAllByDriverId(id))
    }

    @Operation(summary = "Получить конкретную машину водителя", description = "Возвращает детальную информацию по машине")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Машина успешно найдена"),
        ApiResponse(responseCode = "404", description = "Машина или водитель не найдены", content = [Content()])
    ])
    @GetMapping("/{driverId}/cars/{carId}")
    fun getCar(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable driverId: UUID,
        @Parameter(description = "ID машины", example = "987b6543-f21a-43e5-b789-123456789abc") @PathVariable carId: UUID
    ): ResponseEntity<CarResponseDto> {
        return ResponseEntity.ok(carService.findByCarIdAndDriverId(carId, driverId))
    }

    @Operation(summary = "Сделать машину основной", description = "Выбирает конкретную машину для поездок на смене")
    @PatchMapping("/{driverId}/cars/{carId}/main")
    fun assignCarAsMain(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable driverId: UUID,
        @Parameter(description = "ID машины", example = "987b6543-f21a-43e5-b789-123456789abc") @PathVariable carId: UUID
    ): ResponseEntity<CarResponseDto> {
        return ResponseEntity.ok(driverService.assignCarAsMain(driverId, carId))
    }

    @Operation(summary = "Обновить параметры машины", description = "Изменение номера, цвета или характеристик автомобиля")
    @PatchMapping("/{driverId}/cars/{carId}")
    fun updateCar(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable driverId: UUID,
        @Parameter(description = "ID машины", example = "987b6543-f21a-43e5-b789-123456789abc") @PathVariable carId: UUID,
        @Valid @RequestBody dto: UpdateCarDto
    ): ResponseEntity<CarResponseDto> {
        return ResponseEntity.ok(carService.update(driverId, carId, dto))
    }

    @Operation(summary = "Удалить/отвязать машину", description = "Удаляет автомобиль из профиля водителя")
    @DeleteMapping("/{driverId}/cars/{carId}")
    fun deleteCar(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable driverId: UUID,
        @Parameter(description = "ID машины", example = "987b6543-f21a-43e5-b789-123456789abc") @PathVariable carId: UUID
    ): ResponseEntity<Void> {
        driverService.unlinkCar(driverId, carId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Выйти на смену (Доступен)", description = "Переводит статус водителя в режим поиска заказов (AVAILABLE)")
    @PatchMapping("/{id}/duty/start")
    fun startDuty(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID
    ): ResponseEntity<Void> {
        driverService.setWorkStatus(id, WorkStatus.AVAILABLE)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Уйти со смены (Занят/Оффлайн)", description = "Переводит статус водителя в нерабочий режим (OFF_DUTY)")
    @PatchMapping("/{id}/duty/stop")
    fun stopDuty(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID
    ): ResponseEntity<Void> {
        driverService.setWorkStatus(id, WorkStatus.OFF_DUTY)
        return ResponseEntity.noContent().build()
    }

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