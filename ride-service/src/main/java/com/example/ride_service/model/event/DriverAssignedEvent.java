package com.example.ride_service.model.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DriverAssignedEvent(
        UUID rideId,
        UUID driverId,
        String driverName,
        UUID carId,
        String carModel,
        String carColor,
        String carBrand,
        String carLicensePlate,
        Integer seats
) {
}
