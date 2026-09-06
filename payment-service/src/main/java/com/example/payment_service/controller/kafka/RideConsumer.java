package com.example.payment_service.controller.kafka;


import com.example.payment_service.service.handler.PaymentHandler;
import com.example.payment_service.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@SuppressWarnings({"rawtypes", "unchecked"})
@RequiredArgsConstructor
public class RideConsumer {
    private final JsonConverter jsonConverter;
    private final Map<String, PaymentHandler> paymentHandlerMap;

    @KafkaListener(topics = "${spring.kafka.topic.rides.lifecycle}")
    public void listen(@Payload String payload,
                       @Header("eventType") String eventTypeString) {
        try {
            log.info("Received {} event", eventTypeString);

            var handler = paymentHandlerMap.get(eventTypeString);
            var event = jsonConverter.fromJson(payload, handler.getEventType());
            handler.handle(event);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + eventTypeString);
        }
    }
}