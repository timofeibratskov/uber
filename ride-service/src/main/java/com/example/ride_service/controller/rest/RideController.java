package com.example.ride_service.controller.rest;

import com.example.ride_service.model.dto.RideCancelRequestDto;
import com.example.ride_service.model.dto.RideCreateRequestDto;
import com.example.ride_service.model.dto.RideCreateResponseDto;
import com.example.ride_service.model.dto.RideEndResponseDto;
import com.example.ride_service.model.dto.RideEstimateRequestDto;
import com.example.ride_service.model.dto.RideEstimateResponseDto;
import com.example.ride_service.model.dto.RideFullResponseDto;
import com.example.ride_service.service.RidePriceCalculator;
import com.example.ride_service.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Поездки", description = "Управление жизненным циклом поездки: расчет стоимости, заказ, отмена и изменение статусов")
@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;
    private final RidePriceCalculator ridePriceCalculator;

    @Operation(summary = "Получить информацию о поездке", description = "Возвращает полные данные о поездке по ее уникальному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о поездке успешно получена"),
            @ApiResponse(responseCode = "404", description = "Поездка с таким ID не найдена", content = @Content)
    })
    @GetMapping("/{rideId}")
    public ResponseEntity<RideFullResponseDto> getRide(
            @Parameter(description = "Уникальный идентификатор поездки", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
            @PathVariable UUID rideId) {
        return ResponseEntity.ok(rideService.findById(rideId));
    }

    @Operation(summary = "Предварительный расчет стоимости", description = "Рассчитывает ориентировочную цену поездки на основе маршрута и тарифа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Стоимость успешно рассчитана"),
            @ApiResponse(responseCode = "400", description = "Некорректные координаты или данные маршрута", content = @Content)
    })
    @PostMapping("/estimates")
    public ResponseEntity<RideEstimateResponseDto> calculatePrice(@Valid @RequestBody RideEstimateRequestDto request) {
        return ResponseEntity.ok(ridePriceCalculator.calculatePrice(request));
    }

    @Operation(summary = "Создать (заказать) поездку", description = "Регистрирует новую поездку в системе и запускает поиск водителя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Поездка успешно создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных запроса", content = @Content),
            @ApiResponse(responseCode = "409", description = "У пассажира уже есть активная поездка", content = @Content)
    })
    @PostMapping
    public ResponseEntity<RideCreateResponseDto> create(@Valid @RequestBody RideCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.create(request));
    }

    @Operation(summary = "Отменить поездку", description = "Отменяет поездку на этапе поиска водителя или ожидания с указанием причины")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Поездка успешно отменена"),
            @ApiResponse(responseCode = "400", description = "Нельзя отменить поездку на текущем статусе", content = @Content),
            @ApiResponse(responseCode = "404", description = "Поездка не найдена", content = @Content)
    })
    @PostMapping("/{rideId}/cancellation")
    public ResponseEntity<Void> cancel(
            @Parameter(description = "Уникальный идентификатор поездки", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
            @PathVariable UUID rideId,
            @Valid @RequestBody RideCancelRequestDto request) {
        rideService.cancel(rideId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Начать поездку", description = "Переводит поездку в статус 'В пути' (водитель нажал кнопку начала поездки)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Поездка успешно переведена в статус 'В пути'"),
            @ApiResponse(responseCode = "404", description = "Поездка не найдена", content = @Content)
    })
    @PostMapping("/{rideId}/start")
    public ResponseEntity<Void> start(
            @Parameter(description = "Уникальный идентификатор поездки", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
            @PathVariable UUID rideId) {
        rideService.start(rideId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Завершить поездку", description = "Фиксирует окончание поездки, производит финальный расчет и списывает оплату")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Поездка успешно завершена"),
            @ApiResponse(responseCode = "404", description = "Поездка не найдена", content = @Content)
    })
    @PostMapping("/{rideId}/completion")
    public ResponseEntity<RideEndResponseDto> complete(
            @Parameter(description = "Уникальный идентификатор поездки", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
            @PathVariable UUID rideId) {
        return ResponseEntity.ok(rideService.complete(rideId));
    }
}