package com.example.payment_service.controller.rest;

import com.example.payment_service.model.dto.CreateDriverAccountRequest;
import com.example.payment_service.service.DriverAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Счета водителей", description = "Управление платежными и расчетными счетами водителей")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final DriverAccountService driverAccountService;

    @Operation(
            summary = "Создать счет для водителя",
            description = "Регистрирует новый финансовый счет в платежной системе для проведения расчетов с водителем"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Счет успешно создан",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(implementation = String.class, example = "Driver account created successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные входные данные или ошибка валидации DTO",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Счет для данного водителя уже существует",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<String> createAccount(@RequestBody @Valid CreateDriverAccountRequest request) {
        driverAccountService.create(request);
        return ResponseEntity.status(201).body("Driver account created successfully");
    }
}
