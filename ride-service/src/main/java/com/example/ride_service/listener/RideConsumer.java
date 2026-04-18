package com.example.ride_service.listener;

import com.example.ride_service.model.enums.EventType;
import com.example.ride_service.model.enums.TopicType;
import com.example.ride_service.model.event.DriverAssignedEvent;
import com.example.ride_service.service.RideService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideConsumer {
    private final ObjectMapper objectMapper;
    private final RideService rideService;

    @KafkaListener(topics = TopicType.RIDE_LIFECYCLE_TOPIC)
    public void listen(String payload, @Header("eventType") String eventTypeString) {
        try {
            EventType eventType = EventType.fromEventName(eventTypeString);

            if (eventType == EventType.ASSIGNED_DRIVER) {
                log.info("Received {} event", eventType.getEventName());
                var assignDriverEvent = objectMapper.readValue(payload, DriverAssignedEvent.class);
                rideService.acceptRide(assignDriverEvent);
                log.info("Ride with id: {} accepted", assignDriverEvent.rideId());
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + eventTypeString);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}