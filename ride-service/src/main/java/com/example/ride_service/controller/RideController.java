package com.example.ride_service.controller;

import com.example.ride_service.model.dto.RideCreateRequestDto;
import com.example.ride_service.model.dto.RideCreateResponseDto;
import com.example.ride_service.model.dto.RideEstimateRequestDto;
import com.example.ride_service.model.dto.RideEstimateResponseDto;
import com.example.ride_service.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {
    private final RideService rideService;

    @PostMapping("/calculate")
    public ResponseEntity<RideEstimateResponseDto> calculate(@Valid @RequestBody RideEstimateRequestDto request) {
        return ResponseEntity.ok(rideService.calculateRide(request));
    }

    @PostMapping
    public ResponseEntity<RideCreateResponseDto> create(@Valid @RequestBody RideCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.createRide(request));
    }
}