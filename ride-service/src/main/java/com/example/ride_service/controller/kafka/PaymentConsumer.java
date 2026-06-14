package com.example.ride_service.controller.kafka;

import com.example.ride_service.model.enums.EventType;
import com.example.ride_service.model.enums.TopicType;
import com.example.ride_service.service.RideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {
    private final RideService rideService;

    @KafkaListener(topics = TopicType.PAYMENT_TOPIC)
    public void listen(String payload, @Header("eventType") String eventTypeString) {
        try {
            var eventType = EventType.fromEventName(eventTypeString);
            log.info("Received {} event", eventType.getEventName());

            UUID rideId = UUID.fromString(payload);

            switch (eventType) {
                case EventType.PAYMENT_COMPLETED -> rideService.markAsPaid(rideId);

                case EventType.PAYMENT_FAILED -> rideService.markPaymentFailed(rideId);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + eventTypeString);
        }
    }
}