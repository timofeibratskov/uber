package com.example.payment_service.config;

import com.example.payment_service.service.handler.AuthorizePaymentHandler;
import com.example.payment_service.service.handler.CapturePaymentHandler;
import com.example.payment_service.service.handler.PaymentHandler;
import com.example.payment_service.service.handler.ReleasePaymentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentHandlerConfig {
    @Value("${spring.kafka.event.ride-created}")
    private String rideCreatedEventName;
    @Value("${spring.kafka.event.ride-completed}")
    private String rideCompletedEventName;
    @Value("${spring.kafka.event.ride-canceled}")
    private String rideCanceledEventName;

    @Bean
    @SuppressWarnings("rawtypes")
    public Map<String, PaymentHandler> paymentHandlerMap(
            AuthorizePaymentHandler authorizePaymentHandler,
            CapturePaymentHandler capturePaymentHandler,
            ReleasePaymentHandler releasePaymentHandler) {
        return Map.of(
                rideCreatedEventName, authorizePaymentHandler,
                rideCompletedEventName, capturePaymentHandler,
                rideCanceledEventName, releasePaymentHandler);
    }
}
