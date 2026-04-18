package com.example.ride_service.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EventType {
    RIDE_CREATED("ride_create"),
    ASSIGNED_DRIVER("driver_assigned");

    private final String eventName;

    public static EventType fromEventName(String eventName) {
        for (EventType type : EventType.values()) {
            if (type.eventName.equals(eventName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown event name: " + eventName);
    }
}