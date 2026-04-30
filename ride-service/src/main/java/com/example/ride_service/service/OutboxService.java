package com.example.ride_service.service;

import com.example.ride_service.model.entity.OutboxEventEntity;
import com.example.ride_service.model.enums.EventType;
import com.example.ride_service.model.enums.TopicType;
import com.example.ride_service.repo.db.OutboxEventRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxEventRepo outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRateString = "5000")
    public void processOutboxEvents() {
        List<OutboxEventEntity> events = outboxRepo.findAllByOrderByCreatedAt();
        log.info("found {} outbox events", events.size());
        for (OutboxEventEntity event : events) {
            try {
                var record = new ProducerRecord<>(
                        event.getTopic().getTopicName(),
                        event.getId().toString(),
                        event.getPayload()
                );
                record.headers().add("eventType", event.getEventType().getEventName().getBytes());

                kafkaTemplate.send(record).get();

                outboxRepo.deleteById(event.getId());
            } catch (Exception e) {
                log.error("Failed to send event {} to topic {}", event.getId(), event.getTopic());
            }
            log.info("sent event {} to topic {}", event.getId(), event.getTopic());
        }
    }

    @Transactional
    public void saveEvent(Object payload, EventType type, TopicType topic) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            var outbox = OutboxEventEntity.builder()
                    .topic(topic)
                    .eventType(type)
                    .payload(jsonPayload)
                    .createdAt(Instant.now())
                    .build();

            outboxRepo.save(outbox);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Mapping error", e);
        }
    }
}