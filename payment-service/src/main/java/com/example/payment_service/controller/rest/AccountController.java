package com.example.payment_service.controller.rest;

import com.example.payment_service.model.dto.CreateDriverAccountRequest;
import com.example.payment_service.service.DriverAccountService;
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
    private final DriverAccountService driverAccountService;

    @PostMapping
    public ResponseEntity<String> createAccount(@RequestBody @Valid CreateDriverAccountRequest request) {
        driverAccountService.create(request);
        return ResponseEntity.status(201).body("Driver account created successfully");
    }
}
