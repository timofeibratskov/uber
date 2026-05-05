package com.example.payment_service.infrastructure.outbox;

import com.example.payment_service.domain.model.EventType;
import com.example.payment_service.domain.model.TopicType;
import com.example.payment_service.domain.repository.OutboxRepository;
import com.example.payment_service.infrastructure.persistence.entity.OutboxEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepo;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveEvent(Object payload, EventType type, TopicType topic) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            var outbox = OutboxEntity.builder()
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
