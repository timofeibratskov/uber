package com.example.payment_service.controller.kafka;


import com.example.payment_service.model.enums.EventType;
import com.example.payment_service.model.enums.TopicType;
import com.example.payment_service.model.event.RideCanceledEvent;
import com.example.payment_service.model.event.RideCompletedEvent;
import com.example.payment_service.model.event.RideCreatedEvent;
import com.example.payment_service.service.handler.AuthorizePaymentHandler;
import com.example.payment_service.service.handler.CapturePaymentHandler;
import com.example.payment_service.service.handler.ReleasePaymentHandler;
import com.example.payment_service.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @KafkaListener(topics = TopicType.RIDE_LIFECYCLE_TOPIC)
    public void listen(@Payload String payload,
                       @Header("eventType") String eventTypeString) {
        try {
            EventType eventType = EventType.fromEventName(eventTypeString);
            log.info("Received {} event", eventType.getEventName());

            switch (eventType) {
                case RIDE_CREATED -> {
                    var event = jsonConverter.fromJson(payload, RideCreatedEvent.class);
                    authorizePaymentHandler.handle(event);
                }
                case RIDE_COMPLETED -> {
                    var event = jsonConverter.fromJson(payload, RideCompletedEvent.class);
                    capturePaymentHandler.handle(event);
                }
                case RIDE_CANCELED -> {
                    var event = jsonConverter.fromJson(payload, RideCanceledEvent.class);
                    releasePaymentHandler.handle(event);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + eventTypeString);
        }
    }
}