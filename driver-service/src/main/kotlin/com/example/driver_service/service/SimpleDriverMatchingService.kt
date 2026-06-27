package com.example.driver_service.service

import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.model.event.DriverSearchingEvent
import com.example.driver_service.model.event.NoDriversEvent
import com.example.driver_service.model.view.toAssignedDriverEvent
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SimpleDriverMatchingService(
    private val driverService: DriverService,
    private val outboxEventService: OutboxEventService,
    private val locationService: LocationService,
    @param:Value("\${app.kafka.topic.rides.driver-matching}") private val driverMatchingTopic: String,
    @param:Value("\${app.kafka.event.driver-assigned}") private val driverFoundEventType: String,
    @param:Value("\${app.kafka.event.no-drivers}") private val driverNotFoundEventType: String

) : DriverMatchingService {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @Transactional
    override fun findBestDriver(event: DriverSearchingEvent) {
        val ids = locationService.getAvailableIds(event.startPoint)
        if (ids.isEmpty()) {
            val noDriversEvent = NoDriversEvent(
                event.rideId,
                "available drivers not found",
            )
            outboxEventService.saveEvent(
                noDriversEvent,
                driverNotFoundEventType,
                driverMatchingTopic
            )
            log.warn { "no available drivers found" }
        } else {
            val candidates = driverService.findAllAvailableDrivers(ids, event.seats)
            val bestCandidate = candidates[0]
            log.info { "Found: ${bestCandidate.id}, with seats: ${event.seats} from candidates: ${candidates.size}" }

            outboxEventService.saveEvent(
                bestCandidate.toAssignedDriverEvent(event.rideId),
                driverFoundEventType,
                driverMatchingTopic
            )

            driverService.setWorkStatus(bestCandidate.id, WorkStatus.BUSY)
            log.info { "available driver with id: ${bestCandidate.id} is assigned to ride with id: ${event.rideId}" }
        }
    }
}