package com.example.driver_service.constant

import java.util.UUID

object RedisSchema {
    const val DRIVER_STATUS_PREFIX = "driver:status:"
    const val DRIVER_LOCATIONS_KEY = "driver:locations"
    const val DRIVER_CACHE_PREFIX = "driver:cache:"
    fun driverStatusKey(id: UUID): String = "$DRIVER_STATUS_PREFIX$id"
    fun driverCacheKey(id: UUID): String = "$DRIVER_CACHE_PREFIX$id"
}