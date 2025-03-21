package com.example.ride_service.controller;

import com.example.ride_service.dto.RideDto;
import com.example.ride_service.dto.RideRequestDto;
import com.example.ride_service.enums.RideStatus;
import com.example.ride_service.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {
    private final RideService rideService;

    @PostMapping
    public ResponseEntity<RideStatus> createRide(@Valid @RequestBody RideRequestDto request) {
        return ResponseEntity.ok(rideService.createRide(request));
    }

    @GetMapping("/passenger/{id}")
    public List<RideDto> getPassengerRides(@PathVariable Long id) {
        return rideService.getRidesByPassengerId(id);
    }

    @GetMapping("/driver/{userId}")
    public List<RideDto> getDriverRides(@PathVariable Long userId) {
        return rideService.getRidesByDriverId(userId);
    }

    @GetMapping("/status/{status}")
    public List<RideDto> getRidesByStatus(@PathVariable RideStatus status) {
        return rideService.getRidesByStatus(status);
    }

    @PutMapping("/{rideId}/assign-driver")
    public void assignDriver(
            @PathVariable String rideId,
            @RequestParam Long driverId
    ) {
        rideService.assignDriver(rideId, driverId);
    }

    @PutMapping("/{rideId}/start")
    public String startRide(@PathVariable String rideId) {
        return rideService.changeStatus(rideId, RideStatus.IN_PROGRESS);
    }

    @PutMapping("/{rideId}/complete")
    public String completeRide(@PathVariable String rideId) {
        return rideService.changeStatus(rideId, RideStatus.COMPLETED);
    }

    @PutMapping("/{rideId}/pay")
    public String payRide(@PathVariable String rideId) {
        return rideService.changeStatus(rideId, RideStatus.PAID);
    }

    @PatchMapping("/{rideId}/cancel")
    public String cancelRide(@PathVariable String rideId) {
        return rideService.changeStatus(rideId, RideStatus.CANCELLED);
    }
}