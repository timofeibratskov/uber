package com.example.payment_service.infrastructure.web;

import com.example.payment_service.application.dto.CreatePaymentMethodRequest;
import com.example.payment_service.application.dto.UserPaymentMethodResponse;
import com.example.payment_service.application.service.CreatePaymentMethodUseCase;
import com.example.payment_service.application.service.DeletePaymentMethodUseCase;
import com.example.payment_service.application.service.GetPaymentMethodsUseCase;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment-methods")
public class PaymentMethodController {
    private final CreatePaymentMethodUseCase createPaymentMethodUseCase;
    private final GetPaymentMethodsUseCase getPaymentMethodsUseCase;
    private final DeletePaymentMethodUseCase deletePaymentMethodUseCase;

    @GetMapping("/users/{id}")
    public ResponseEntity<List<UserPaymentMethodResponse>> getPaymentMethodByUserId(@PathVariable UUID id) {
        return ResponseEntity.ok(getPaymentMethodsUseCase.findUsersPaymentMethods(id));
    }

    @PostMapping
    public ResponseEntity<String> createPaymentMethod(@RequestBody @Valid CreatePaymentMethodRequest request) {
        createPaymentMethodUseCase.execute(request);
        return ResponseEntity.status(201).body("Payment method created successfully!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable UUID id) {
        deletePaymentMethodUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}