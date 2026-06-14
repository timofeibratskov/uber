package com.example.ride_service.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EventType {
    RIDE_CREATED("ride_create"),
    ASSIGNED_DRIVER("driver_assigned"),
    NO_AVAILABLE_DRIVERS("no_available_drivers"),
    RIDE_CANCELLED("ride_cancelled"),
    PAYMENT_COMPLETED("payment_completed"),
    PAYMENT_FAILED("payment_failed"),
    RIDE_COMPLETED("ride_completed");


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