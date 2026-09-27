package com.example.driver_service.controller.rest

import com.example.driver_service.model.dto.CompleteProfileRequestDto
import com.example.driver_service.model.dto.DriverResponseDto
import com.example.driver_service.model.dto.UpdateDriverDto
import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.service.DriverService
import com.example.driver_service.service.ShiftService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    private val shiftService: ShiftService
) {
    @Operation(
        summary = "Регистрация / заполнение профиля",
        description = "Первичное заполнение личных данных водителя"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Профиль успешно заполнен"),
            ApiResponse(responseCode = "400", description = "Некорректные данные профиля", content = [Content()])
        ]
    )
    @PostMapping("/{id}/profile")
    fun register(
        @Valid @RequestBody dto: CompleteProfileRequestDto,
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID
    ): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(driverService.completeProfile(id, dto))
    }

    @Operation(summary = "Получить профиль водителя", description = "Возвращает информацию о водителе по его ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Профиль успешно получен"),
            ApiResponse(responseCode = "404", description = "Водитель не найден", content = [Content()])
        ]
    )
    @GetMapping("/{id}")
    fun findById(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID
    ): ResponseEntity<DriverResponseDto> {
        return ResponseEntity.ok(driverService.findById(id))
    }

    @Operation(
        summary = "Обновить личные данные водителя",
        description = "Изменение имени, телефона или других полей водителя"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Профиль успешно обновлен"),
            ApiResponse(responseCode = "400", description = "Ошибка валидации", content = [Content()])
        ]
    )
    @PatchMapping("/{id}")
    fun update(
        @Parameter(
            description = "ID водителя",
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable id: UUID,
        @Valid @RequestBody dto: UpdateDriverDto
    ): ResponseEntity<Void> {
        driverService.editProfile(id, dto)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Выйти на смену (Доступен)",
        description = "Переводит статус водителя в режим поиска заказов (AVAILABLE)"
    )
    @PatchMapping("/{id}/shift/start")
    fun startDuty(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID
    ): ResponseEntity<Void> {
        shiftService.start(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Уйти со смены (Занят/Оффлайн)",
        description = "Переводит статус водителя в нерабочий режим (OFF_DUTY)"
    )
    @PatchMapping("/{id}/shift/stop")
    fun stopDuty(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID
    ): ResponseEntity<Void> {
        shiftService.stop(id)
        return ResponseEntity.noContent().build()
    }
}