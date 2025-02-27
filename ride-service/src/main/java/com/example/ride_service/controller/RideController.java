package com.example.ride_service.controller;

import com.example.ride_service.dto.RideRequestDto;
import com.example.ride_service.entity.RideEntity;
import com.example.ride_service.entity.RideStatus;
import com.example.ride_service.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    // Создать поездку
    @PostMapping
    public ResponseEntity<RideEntity> createRide(@Valid @RequestBody RideRequestDto request) {
        return ResponseEntity.ok(rideService.createRide(request));
    }

    // Получить все поездки пользователя
    @GetMapping("/passenger/{id}")
    public List<RideEntity> getPassengerRides(@PathVariable Long id) {
        return rideService.getRidesByPassengerId(id);
    }

    @GetMapping("/driver/{userId}")
    public List<RideEntity> getDriverRides(@PathVariable Long userId) {
        return rideService.getRidesByDriverId(userId);
    }

    @GetMapping("/status/{status}")
    public List<RideEntity> getRidesByStatus(@PathVariable RideStatus status) {
        return rideService.getRidesByStatus(status);
    }

    // Назначить водителя
    @PatchMapping("/{rideId}/assign-driver")
    public RideEntity assignDriver(
            @PathVariable String rideId,
            @RequestParam Long driverId
    ) {
        return rideService.assignDriver(rideId, driverId);
    }

    // Начать поездку
    @PatchMapping("/{rideId}/start")
    public RideEntity startRide(@PathVariable String rideId) {
        return rideService.changeStatus(rideId, RideStatus.IN_PROGRESS);
    }

    // Завершить поездку
    @PatchMapping("/{rideId}/complete")
    public RideEntity completeRide(@PathVariable String rideId) {
        return rideService.changeStatus(rideId, RideStatus.COMPLETED);
    }

    // Оплатить поездку
    @PatchMapping("/{rideId}/pay")
    public RideEntity payRide(@PathVariable String rideId) {
        return rideService.changeStatus(rideId, RideStatus.PAID);
    }

    // Отменить поездку
    @PatchMapping("/{rideId}/cancel")
    public RideEntity cancelRide(@PathVariable String rideId) {
        return rideService.changeStatus(rideId, RideStatus.CANCELLED);
    }
}