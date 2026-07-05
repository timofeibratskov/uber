package com.example.driver_service.controller.listener

import com.example.driver_service.model.event.DriverSearchingEvent
import com.example.driver_service.service.DriverMatchingService
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class PaymentAuthorizationListener(
    private val objectMapper: ObjectMapper,
    private val driverMatchingService: DriverMatchingService,
    @Qualifier("kafkaTypeMapping")
    private val typeMapping: Map<String, Class<out Any>>
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @KafkaListener(topics = ["\${spring.kafka.topic.rides.payment}"])
    fun listen(
        @Payload payload: String,
        @Header("eventType") type: String
    ) {
        try {
            log.info { "Processing event: $type" }
            val targetClass = typeMapping[type]
                ?: throw IllegalArgumentException("Unknown event type: $type")

            when (val event = objectMapper.readValue(payload, targetClass)) {
                is DriverSearchingEvent -> driverMatchingService.findBestDriver(event)
                else -> log.warn { "No handler found for class: ${targetClass.simpleName}" }
            }
        } catch (e: Exception) {
            log.error(e) { "Error while processing kafka message: ${e.message} in driver searching listener!" }
        }
    }
}