package com.example.driver_service.service

import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.model.view.DriverView
import mu.KotlinLogging
import org.springframework.data.geo.Point
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SimpleDriverMatchingService(
    private val driverService: DriverService,
    private val locationService: LocationService,
) : DriverMatchingService {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @Transactional
    override fun findBestDriver(
        point: Point,
        seats: Int
    ): DriverView? {
        val ids = locationService.getAvailableIds(point)
        if (ids.isEmpty()) return null

        val candidates = driverService.findAllAvailableDrivers(ids, seats)
        val bestCandidate = candidates[0]

        driverService.setWorkStatus(bestCandidate.id, WorkStatus.BUSY)
        log.info { "Found: ${bestCandidate.id}, with seats: $seats" }

        return bestCandidate
    }
}