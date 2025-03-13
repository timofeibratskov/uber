package com.example.driver_service.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

@FeignClient(    name = "ride-service",
    url = "http://localhost:8083/api/rides" )
interface RideServiceClient {

    @PutMapping("/{rideId}/assign-driver")
    fun assignDriver(
        @PathVariable rideId: String,
        @RequestParam driverId: Long
    )
}