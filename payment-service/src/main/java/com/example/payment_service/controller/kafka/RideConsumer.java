package com.example.payment_service.controller.kafka;


import com.example.payment_service.model.event.RideCanceledEvent;
import com.example.payment_service.model.event.RideCompletedEvent;
import com.example.payment_service.model.event.RideCreatedEvent;
import com.example.payment_service.service.handler.AuthorizePaymentHandler;
import com.example.payment_service.service.handler.CapturePaymentHandler;
import com.example.payment_service.service.handler.ReleasePaymentHandler;
import com.example.payment_service.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideConsumer {
    private final JsonConverter jsonConverter;
    private final AuthorizePaymentHandler authorizePaymentHandler;
    private final CapturePaymentHandler capturePaymentHandler;
    private final ReleasePaymentHandler releasePaymentHandler;
    @Value("${spring.kafka.event.ride-created}")
    private String rideCreatedEventName;
    @Value("${spring.kafka.event.ride-canceled}")
    private String rideCanceledEventName;
    @Value("${spring.kafka.event.ride-completed}")
    private String rideCompletedEventName;

    @KafkaListener(topics = "${spring.kafka.topic.rides.lifecycle}")
    public void listen(@Payload String payload,
                       @Header("eventType") String eventTypeString) {
        try {
            log.info("Received {} event", eventTypeString);

            if (eventTypeString.equals(rideCreatedEventName)) {
                var event = jsonConverter.fromJson(payload, RideCreatedEvent.class);
                authorizePaymentHandler.handle(event);


            } else if (eventTypeString.equals(rideCompletedEventName)) {
                var event = jsonConverter.fromJson(payload, RideCompletedEvent.class);
                capturePaymentHandler.handle(event);


            } else if (eventTypeString.equals(rideCanceledEventName)) {
                var event = jsonConverter.fromJson(payload, RideCanceledEvent.class);
                releasePaymentHandler.handle(event);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + eventTypeString);
        }
    }
}