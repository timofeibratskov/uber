package com.example.driver_service.listener

import com.example.driver_service.model.enums.EventType
import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.model.event.RideCreateEvent
import com.example.driver_service.service.DriverMatchingService
import com.example.driver_service.service.DriverService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class RideConsumer(
    private val objectMapper: ObjectMapper,
    private val driverMatchingService: DriverMatchingService,
    private val driverService: DriverService
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @KafkaListener(topics = ["\${app.Kafka.ride-topic}"])
    fun listen(
        payload: String,
        @Header("eventType") type: String
    ) {
        try {
            log.info { "Processing event: $type" }
            when (type) {
                EventType.RIDE_CREATED.eventName -> {
                    val event = objectMapper.readValue<RideCreateEvent>(payload)
                    val driver = driverMatchingService.findBestDriver(event.startPoint, event.seats)
                    if (driver != null) {
                        driverService.setWorkStatus(driver.id, WorkStatus.BUSY)
                        log.info { "available driver with id: ${driver.id} is assigned to ride with id: ${event.rideId}" }
                    } else
                        log.info { "available drivers not found!" }
                }
            }
        } catch (e: Exception) {
            log.error(e) { "Error while processing kafka message: ${e.message}" }
        }
    }
}