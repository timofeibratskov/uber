package com.example.driver_service.controller.rest

import com.example.driver_service.model.dto.ShiftDto
import com.example.driver_service.service.ShiftService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Смена", description = "Управление сменами")
@RestController
@RequestMapping("/api/v1/drivers")
class ShiftController(
    private val shiftService: ShiftService,
) {
    @Operation(
        summary = "Выйти на смену (Доступен)",
        description = "Переводит статус водителя в режим поиска заказов (AVAILABLE)"
    )
    @PostMapping("/{id}/shift/start")
    fun startShift(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID
    ): ResponseEntity<Void> {
        shiftService.start(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Получить актуальную смену",
        description = "Возвращает актуальную незакрытую смену или пустой ответ"
    )
    @GetMapping("/{id}/shift/actual")
    fun getOpenedShift(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID
    ): ResponseEntity<ShiftDto> {
        val shift = shiftService.getActual(id)
        return if (shift != null)
            ResponseEntity.ok(shift)
        else
            ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Уйти со смены (Занят/Оффлайн)",
        description = "Переводит статус водителя в нерабочий режим (OFF_DUTY)"
    )
    @PatchMapping("/{id}/shift/stop")
    fun stopShift(
        @Parameter(description = "ID водителя", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable id: UUID
    ): ResponseEntity<ShiftDto> {
        return ResponseEntity.ok(shiftService.stop(id))
    }
}