package com.example.ride_service.controller.kafka;

import com.example.ride_service.mapper.RideMapper;
import com.example.ride_service.model.event.DriverAssignedEvent;
import com.example.ride_service.model.event.NoDriversEvent;
import com.example.ride_service.service.RideService;
import com.example.ride_service.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class DriverMatchingConsumer {
    private final RideMapper rideMapper;
    private final RideService rideService;
    private final JsonConverter jsonConverter;

    @Value("${spring.kafka.event.driver-assigned}")
    private String driverAssignedEventName;
    @Value("${spring.kafka.event.no-drivers}")
    private String NoDriversEventName;

    @KafkaListener(topics = "${spring.kafka.topic.rides.driver-matching}")
    public void listen(String payload,
                       @Header("eventType") String eventTypeString) {
        try {
            log.info("Received {} event", eventTypeString);

            if (driverAssignedEventName.equals(eventTypeString)) {
                var assignDriverEvent = jsonConverter.fromJson(payload, DriverAssignedEvent.class);
                rideService.accept(assignDriverEvent);

            } else if (NoDriversEventName.equals(eventTypeString)) {
                var noAvailableDriversEvent = jsonConverter.fromJson(payload, NoDriversEvent.class);
                rideService.cancel(noAvailableDriversEvent.rideId(),
                        rideMapper.toRideCancelRequestDto(noAvailableDriversEvent));
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + eventTypeString);
        }
    }
}