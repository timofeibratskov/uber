package com.example.rating_service.controller.kafka;

import com.example.rating_service.model.events.UserCreatedEvent;
import com.example.rating_service.service.RatingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class UserCreatedEventListener {
    private final ObjectMapper objectMapper;
    private final RatingService service;

    @Value("${spring.kafka.event.user-created}")
    private String userCreatedEventName;

    @KafkaListener(topics = "${spring.kafka.topic.rides.users}")
    public void listen(String payload, @Header("eventType") String eventTypeString) {
        try {
            log.info("Received {} event", eventTypeString);
            if (eventTypeString.equals(userCreatedEventName)) {
                var event = objectMapper.readValue(payload, UserCreatedEvent.class);
                service.createUser(event.userId());
            }
        } catch (JsonProcessingException e) {
            log.error("Json converting exception: {}", e.getMessage());
            throw new RuntimeException("Json converting exception", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + eventTypeString);
        }
    }
}
