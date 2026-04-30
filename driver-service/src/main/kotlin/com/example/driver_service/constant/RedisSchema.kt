package com.example.driver_service.constant

import java.util.UUID

object RedisSchema {
    const val DRIVER_STATUS_PREFIX = "driver:status:"
    const val DRIVER_LOCATIONS_KEY = "driver:locations"
    fun driverStatusKey(id: UUID): String = "$DRIVER_STATUS_PREFIX$id"
}