package com.example.passenger_service.handler.rest;

import com.example.passenger_service.model.dto.CompleteProfileRequestDto;
import com.example.passenger_service.model.dto.FavoriteAddressRequestDto;
import com.example.passenger_service.model.dto.FavoriteAddressResponseDto;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.UpdatePassengerDto;
import com.example.passenger_service.service.FavoriteAddressService;
import com.example.passenger_service.service.PassengerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Пассажиры", description = "Управление профилями пассажиров и их избранными адресами")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/passengers")
public class PassengerController {

    private final PassengerService passengerService;
    private final FavoriteAddressService favoriteAddressService;

    @Operation(summary = "Заполнение профиля пассажира", description = "Позволяет новому пассажиру заполнить личные данные")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Профиль успешно заполнен",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пассажир не найден", content = @Content)
    })
    @PostMapping("/{id}/profile")
    public ResponseEntity<String> completeProfile(
            @RequestBody @Valid CompleteProfileRequestDto request,
            @Parameter(description = "Уникальный идентификатор пассажира", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        return ResponseEntity.status(201).body(passengerService.completeProfile(id, request));
    }

    @Operation(summary = "Получить пассажира по ID", description = "Возвращает полную информацию о пассажире по его UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пассажир успешно найден"),
            @ApiResponse(responseCode = "404", description = "Пассажир с таким ID не существует", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PassengerResponseDto> getPassengerById(
            @Parameter(description = "Идентификатор пассажира", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        return ResponseEntity.ok().body(passengerService.findPassengerById(id));
    }

    @Operation(summary = "Частичное обновление данных пассажира", description = "Обновляет только переданные поля в профиле пассажира")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Данные успешно обновлены (нет содержимого)"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных", content = @Content),
            @ApiResponse(description = "Пассажир не найден", responseCode = "404", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<String> updatePassenger(
            @Parameter(description = "Идентификатор пассажира", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @RequestBody @Valid UpdatePassengerDto request) {
        passengerService.updatePassenger(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Добавить избранный адрес", description = "Добавляет новый адрес (например, 'Дом', 'Работа') в список любимых мест пассажира")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Адрес успешно добавлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации адреса", content = @Content)
    })
    @PostMapping("/{id}/addresses")
    public ResponseEntity<FavoriteAddressResponseDto> addFavoriteAddress(
            @Parameter(description = "Идентификатор пассажира", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @RequestBody @Valid FavoriteAddressRequestDto request) {
        // Исправлено имя метода (было deleteFavoriteAddress, переименовано согласно логике PostMapping)
        return ResponseEntity.status(201).body(favoriteAddressService.addFavoriteAddress(id, request));
    }

    @Operation(summary = "Получить все избранные адреса", description = "Возвращает список всех сохраненных адресов для конкретного пассажира")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список адресов успешно получен")
    })
    @GetMapping("/{id}/addresses")
    public ResponseEntity<List<FavoriteAddressResponseDto>> getFavoriteAddress(
            @Parameter(description = "Идентификатор пассажира", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        return ResponseEntity.ok().body(favoriteAddressService.getAllAddressesByPassengerId(id));
    }

    @Operation(summary = "Удалить избранный адрес", description = "Удаляет конкретный адрес из списка избранных мест пассажира")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Адрес успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пассажир или адрес не найден", content = @Content)
    })
    @DeleteMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<Void> deleteFavoriteAddress(
            @Parameter(description = "Идентификатор пассажира", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(description = "Идентификатор удаляемого адреса", example = "987b6543-f21a-43e5-b789-123456789abc")
            @PathVariable UUID addressId) {
        favoriteAddressService.removeFavoriteAddress(id, addressId);
        return ResponseEntity.noContent().build();
    }
}
