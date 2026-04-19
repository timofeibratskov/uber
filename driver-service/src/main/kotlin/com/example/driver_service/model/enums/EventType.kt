package com.example.driver_service.model.enums

enum class EventType(val eventName: String) {
    RIDE_CREATED("ride_create"),
    ASSIGNED_DRIVER("driver_assigned"),
    NO_AVAILABLE_DRIVERS("no_available_drivers")
}