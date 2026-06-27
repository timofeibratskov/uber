package com.example.driver_service.service

import com.example.driver_service.model.event.DriverSearchingEvent

interface DriverMatchingService {
    fun findBestDriver(event: DriverSearchingEvent)
}