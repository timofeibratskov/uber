package com.example.passenger_service.handler.kafka;

import com.example.passenger_service.model.events.UserRegisteredEvent;
import com.example.passenger_service.service.PassengerService;
import com.example.passenger_service.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedListener {
    private final PassengerService service;
    private final JsonConverter converter;
    @Value("${spring.kafka.event.user-created}")
    private String userCreatedEventName;

    @KafkaListener(topics = "${spring.kafka.topic.rides.users}")
    public void listen(
            @Payload String payload,
            @Header("eventType") String eventTypeString
    ) {
        try {
            log.info("Received {} event", eventTypeString);
            if (eventTypeString.equals(userCreatedEventName)) {
                var event = converter.fromJson(payload, UserRegisteredEvent.class);
                if (event.type().equals("PASSENGER"))
                    service.save(event);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + eventTypeString);
        }
    }
}