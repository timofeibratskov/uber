package com.example.payment_service.infrastructure.web;

import com.example.payment_service.application.dto.CreateDriverAccountRequest;
import com.example.payment_service.application.service.CreateDriverAccountUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final CreateDriverAccountUseCase createDriverAccountUseCase;

    @PostMapping
    public ResponseEntity<String> process(@RequestBody @Valid CreateDriverAccountRequest request) {
        createDriverAccountUseCase.execute(request);
        return ResponseEntity.ok("Driver account created successfully");
    }
}
