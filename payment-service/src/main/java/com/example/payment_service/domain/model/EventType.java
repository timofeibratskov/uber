package com.example.payment_service.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EventType {
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