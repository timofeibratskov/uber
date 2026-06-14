package com.example.ride_service.controller.kafka;

import com.example.ride_service.mapper.RideMapper;
import com.example.ride_service.model.enums.EventType;
import com.example.ride_service.model.enums.TopicType;
import com.example.ride_service.model.event.DriverAssignedEvent;
import com.example.ride_service.model.event.NoDriversEvent;
import com.example.ride_service.service.RideService;
import com.example.ride_service.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideConsumer {
    private final RideMapper rideMapper;
    private final RideService rideService;
    private final JsonConverter jsonConverter;

    @KafkaListener(topics = TopicType.RIDE_LIFECYCLE_TOPIC)
    public void listen(String payload,
                       @Header("eventType") String eventTypeString) {
        try {
            EventType eventType = EventType.fromEventName(eventTypeString);
            log.info("Received {} event", eventType.getEventName());

            switch (eventType) {
                case ASSIGNED_DRIVER -> {
                    var assignDriverEvent = jsonConverter.fromJson(payload, DriverAssignedEvent.class);
                    rideService.accept(assignDriverEvent);
                }

                case NO_AVAILABLE_DRIVERS -> {
                    var noAvailableDriversEvent = jsonConverter.fromJson(payload, NoDriversEvent.class);
                    rideService.cancel(noAvailableDriversEvent.rideId(),
                            rideMapper.toRideCancelRequestDto(noAvailableDriversEvent));
                }
            }
        } catch (
                IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + eventTypeString);
        }
    }
}