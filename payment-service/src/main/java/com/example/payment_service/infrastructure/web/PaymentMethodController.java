package com.example.payment_service.infrastructure.web;

import com.example.payment_service.application.dto.CreatePaymentMethodRequest;
import com.example.payment_service.application.service.CreatePaymentMethodUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment-methods")
public class PaymentMethodController {
    private final CreatePaymentMethodUseCase createPaymentMethodUseCase;

    @PostMapping
    public ResponseEntity<String> createPaymentMethod(@RequestBody @Valid CreatePaymentMethodRequest request) {
        createPaymentMethodUseCase.execute(request);
        return ResponseEntity.status(201).body("Payment method created successfully!");
    }
}