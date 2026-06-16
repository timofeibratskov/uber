package com.example.payment_service.infrastructure.kafka;


import com.example.payment_service.application.dto.CreatePaymentRequest;
import com.example.payment_service.application.event.RideCompletedEvent;
import com.example.payment_service.application.service.ProcessPaymentUseCase;
import com.example.payment_service.domain.model.EventType;
import com.example.payment_service.domain.model.TopicType;
import com.example.payment_service.domain.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideConsumer {
    private final JsonConverter jsonConverter;
    private final ProcessPaymentUseCase processPaymentUseCase;

    @KafkaListener(topics = TopicType.RIDE_LIFECYCLE_TOPIC)
    public void listen(String payload,
                       @Header("eventType") String eventTypeString) {
        try {
            EventType eventType = EventType.fromEventName(eventTypeString);
            log.info("Received {} event", eventType.getEventName());

            if (eventType == EventType.RIDE_COMPLETED) {
                var event = jsonConverter.fromJson(payload, RideCompletedEvent.class);

                var request = CreatePaymentRequest.builder()
                        .passengerId(event.passengerId())
                        .driverId(event.driverId()).
                        rideId(event.rideId()).
                        amount(event.amount()).
                        currency(event.currency()).
                        paymentMethodId(event.paymentMethodId())
                        .build();

                processPaymentUseCase.execute(request);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + eventTypeString);
        }
    }
}