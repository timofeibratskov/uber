package com.example.payment_service.infrastructure.web;

import com.example.payment_service.application.dto.PaymentRequest;
import com.example.payment_service.application.service.ProcessPaymentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final ProcessPaymentUseCase processPaymentUseCase;

    @PostMapping("/process")
    public ResponseEntity<String> process(@RequestBody PaymentRequest request) {
        processPaymentUseCase.execute(request);
        return ResponseEntity.ok("Payment processed successfully");
    }
}