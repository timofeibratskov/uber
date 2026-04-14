package com.example.ride_service.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EventType {
    RIDE_CREATED("ride_create");

    private final String eventName;
}