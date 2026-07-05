package com.example.ride_service.controller.kafka;

import com.example.ride_service.model.dto.RideCancelRequestDto;
import com.example.ride_service.model.event.AuthorizeFailedEvent;
import com.example.ride_service.model.event.PaymentCaptureFailedEvent;
import com.example.ride_service.model.event.PaymentCapturedEvent;
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
public class PaymentConsumer {
    private final RideService rideService;
    private final JsonConverter jsonConverter;

    @Value("${spring.kafka.event.payment-authorization-failed}")
    private String paymentAuthFailedEventName;
    @Value("${spring.kafka.event.payment-captured}")
    private String paymentCapturedEventName;
    @Value("${spring.kafka.event.payment-capture-failed}")
    private String paymentCapturedFailedEventName;

    @KafkaListener(topics = "${spring.kafka.topic.rides.payment}")
    public void listen(String payload, @Header("eventType") String eventTypeString) {
        try {
            log.info("Received {} event", eventTypeString);

            if (eventTypeString.equals(paymentCapturedEventName)) {
                var event = jsonConverter.fromJson(payload, PaymentCapturedEvent.class);
                rideService.markAsPaid(event.rideId());

            } else if (eventTypeString.equals(paymentCapturedFailedEventName)) {
                var event = jsonConverter.fromJson(payload, PaymentCaptureFailedEvent.class);
                rideService.markPaymentFailed(event.rideId());

            } else if (eventTypeString.equals(paymentAuthFailedEventName)) {
                var event = jsonConverter.fromJson(payload, AuthorizeFailedEvent.class);
                rideService.cancel(
                        event.rideId(),
                        RideCancelRequestDto.builder()
                                .comment("Insufficient funds")
                                .build()
                );
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + eventTypeString);
        }
    }
}