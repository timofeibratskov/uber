package com.example.passenger_service.controller;

import com.example.passenger_service.model.dto.LoginPassengerDto;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.RegisterPassengerDto;
import com.example.passenger_service.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


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

    @PostMapping("/login")
    public ResponseEntity<PassengerResponseDto> loginPassenger(
            @RequestBody @Valid LoginPassengerDto request) {
        return ResponseEntity.ok().body(passengerService.loginPassenger(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerResponseDto> getPassengerById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(passengerService.findPassengerById(id));
    }
}