package com.example.driver_service.service

import com.example.driver_service.model.entity.OutboxEventEntity
import com.example.driver_service.model.enums.EventType
import com.example.driver_service.repository.OutboxEventRepository
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutboxEventService(
    private val outboxRepo: OutboxEventRepository,
    private val objectMapper: ObjectMapper,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @Scheduled(fixedRateString = "5000")
    fun processOutboxEvents() {
        val events = outboxRepo.findAllByOrderByCreatedAt()

        log.info { "found ${events.size} outbox events" }
        for (event in events) {
            try {
                val record = ProducerRecord<String, String>(
                    event.topic,
                    event.id.toString(),
                    event.payload
                )
                record.headers().add("eventType", event.eventType!!.eventName.toByteArray())

                kafkaTemplate.send(record).get()

                outboxRepo.deleteById(event.id!!)
            } catch (e: Exception) {
                log.error { "error: ${e.message}" }
                log.error { "Failed to send event ${event.id} to topic ${event.topic}" }
            }
            log.info { "sent event ${event.id} to topic ${event.topic}" }
        }
    }

    @Transactional
    fun saveEvent(payload: Any, type: EventType, topic: String) {
        try {
            val jsonPayload = objectMapper.writeValueAsString(payload)

            val outbox = OutboxEventEntity(
                topic = topic,
                eventType = type,
                payload = jsonPayload
            )

            outboxRepo.save(outbox)
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Mapping error", e)
        }
    }
}