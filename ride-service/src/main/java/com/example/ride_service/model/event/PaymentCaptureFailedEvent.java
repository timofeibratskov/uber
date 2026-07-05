package com.example.ride_service.model.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PaymentCaptureFailedEvent(
        UUID rideId,
        UUID paymentId,
        UUID transactionId,
        String errorMessage
) {
}
