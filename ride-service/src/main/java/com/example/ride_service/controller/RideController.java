package com.example.ride_service.controller;

import com.example.ride_service.model.dto.RideCancelRequestDto;
import com.example.ride_service.model.dto.RideCreateRequestDto;
import com.example.ride_service.model.dto.RideCreateResponseDto;
import com.example.ride_service.model.dto.RideEndResponseDto;
import com.example.ride_service.model.dto.RideEstimateRequestDto;
import com.example.ride_service.model.dto.RideEstimateResponseDto;
import com.example.ride_service.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @PostMapping("/calculate")
    public ResponseEntity<RideEstimateResponseDto> calculate(@Valid @RequestBody RideEstimateRequestDto request) {
        return ResponseEntity.ok(rideService.calculateRide(request));
    }

    @PostMapping
    public ResponseEntity<RideCreateResponseDto> create(@Valid @RequestBody RideCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.createRide(request));
    }

    @PatchMapping("/{rideId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID rideId,
                                       @Valid @RequestBody RideCancelRequestDto request) {
        rideService.cancelRide(rideId, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{rideId}/start")
    public ResponseEntity<Void> start(@PathVariable UUID rideId) {
        rideService.startRide(rideId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{rideId}/end")
    public ResponseEntity<RideEndResponseDto> end(@PathVariable UUID rideId) {
        return ResponseEntity.ok(rideService.endRide(rideId));
    }
}