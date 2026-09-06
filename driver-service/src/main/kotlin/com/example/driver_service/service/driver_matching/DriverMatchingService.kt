package com.example.driver_service.service.driver_matching

import com.example.driver_service.model.event.PaymentAuthorizedEvent

interface DriverMatchingService {
    fun findBestDriver(event: PaymentAuthorizedEvent)
}