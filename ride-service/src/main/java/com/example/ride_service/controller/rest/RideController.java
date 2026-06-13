package com.example.ride_service.controller.rest;

import com.example.ride_service.model.dto.RideCancelRequestDto;
import com.example.ride_service.model.dto.RideCreateRequestDto;
import com.example.ride_service.model.dto.RideCreateResponseDto;
import com.example.ride_service.model.dto.RideEndResponseDto;
import com.example.ride_service.model.dto.RideEstimateRequestDto;
import com.example.ride_service.model.dto.RideEstimateResponseDto;
import com.example.ride_service.model.dto.RideFullResponseDto;
import com.example.ride_service.service.RidePriceCalculator;
import com.example.ride_service.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {
    private final RideService rideService;
    private final RidePriceCalculator ridePriceCalculator;

    @GetMapping("/{rideId}")
    public ResponseEntity<RideFullResponseDto> getRide(@PathVariable UUID rideId) {
        return ResponseEntity.ok(rideService.findById(rideId));
    }

    @PostMapping("/estimates")
    public ResponseEntity<RideEstimateResponseDto> calculatePrice(@Valid @RequestBody RideEstimateRequestDto request) {
        return ResponseEntity.ok(ridePriceCalculator.calculatePrice(request));
    }

    @PostMapping
    public ResponseEntity<RideCreateResponseDto> create(@Valid @RequestBody RideCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.create(request));
    }

    @PostMapping("/{rideId}/cancellation")
    public ResponseEntity<Void> cancel(@PathVariable UUID rideId,
                                       @Valid @RequestBody RideCancelRequestDto request) {
        rideService.cancel(rideId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{rideId}/start")
    public ResponseEntity<Void> start(@PathVariable UUID rideId) {
        rideService.start(rideId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{rideId}/completion")
    public ResponseEntity<RideEndResponseDto> complete(@PathVariable UUID rideId) {
        return ResponseEntity.ok(rideService.complete(rideId));
    }
}