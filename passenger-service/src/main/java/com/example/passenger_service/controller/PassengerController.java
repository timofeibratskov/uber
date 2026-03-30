package com.example.passenger_service.controller;

import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.RegisterPassengerDto;
import com.example.passenger_service.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/passengers")
public class PassengerController {
    private final PassengerService passengerService;

    @PostMapping("/register")
    public ResponseEntity<PassengerResponseDto> registerPassenger(
            @RequestBody @Valid RegisterPassengerDto request) {
        return ResponseEntity.status(201).body(passengerService.registerPassenger(request));
    }
}