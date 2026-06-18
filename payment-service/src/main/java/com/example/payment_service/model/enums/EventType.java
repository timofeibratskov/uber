package com.example.payment_service.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EventType {
    PAYMENT_AUTHORIZED("payment_authorized"),
    PAYMENT_AUTHORIZATION_FAILED("payment_authorization_failed"),

    PAYMENT_COMPLETED("payment_completed"),
    PAYMENT_CAPTURE_FAILED("payment_capture_failed"),

    PAYMENT_RELEASED("payment_released"),
    PAYMENT_RELEASE_FAILED("payment_release_failed"),

    RIDE_COMPLETED("ride_completed"),
    RIDE_CREATED("ride_created"),
    RIDE_CANCELED("ride_canceled");

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