package com.example.driver_service.service

import com.example.driver_service.model.view.DriverView
import org.springframework.data.geo.Point

interface DriverMatchingService {
    fun findBestDriver(point: Point, seats: Int): DriverView?
}