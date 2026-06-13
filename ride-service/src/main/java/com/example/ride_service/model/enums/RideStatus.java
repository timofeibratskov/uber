package com.example.ride_service.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RideStatus {

    CREATED("Ride created"),
    SEARCHING("Searching drivers"),
    ACCEPTED("Driver coming"),
    STARTED("On the way"),
    COMPLETED("Arrived"),
    CANCELLED("Cancelled");

    private final String description;
}