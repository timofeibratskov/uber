package com.example.payment_service.controller.rest;

import com.example.payment_service.model.dto.CreatePaymentMethodRequest;
import com.example.payment_service.model.dto.UserPaymentMethodResponse;
import com.example.payment_service.service.PaymentMethodService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Способы оплаты", description = "Управление привязанными банковскими картами и кошельками пользователей")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @Operation(
            summary = "Получить способы оплаты пользователя",
            description = "Возвращает список всех сохраненных платежных методов (карт) конкретного пользователя по его ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список способов оплаты успешно получен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<List<UserPaymentMethodResponse>> getPaymentMethodByUserId(
            @Parameter(description = "Уникальный идентификатор пользователя (пассажира или водителя)", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(paymentMethodService.findAllByUserId(id));
    }

    @Operation(
            summary = "Привязать новый способ оплаты",
            description = "Регистрирует новую банковскую карту или кошелек для последующей оплаты поездок"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Способ оплаты успешно создан",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(implementation = String.class, example = "Payment method created successfully!")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных", content = @Content),
            @ApiResponse(responseCode = "409", description = "Такой способ оплаты уже привязан к пользователю", content = @Content)
    })
    @PostMapping
    public ResponseEntity<String> createPaymentMethod(@RequestBody @Valid CreatePaymentMethodRequest request) {
        paymentMethodService.create(request);
        return ResponseEntity.status(201).body("Payment method created successfully!");
    }

    @Operation(
            summary = "Удалить способ оплаты",
            description = "Отвязывает банковскую карту или кошелек по уникальному идентификатору платежного метода"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Способ оплаты успешно удален (нет содержимого)"),
            @ApiResponse(responseCode = "404", description = "Указанный способ оплаты не найден", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentMethod(
            @Parameter(description = "Идентификатор привязанного способа оплаты", example = "987b6543-f21a-43e5-b789-123456789abc")
            @PathVariable UUID id) {
        paymentMethodService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
