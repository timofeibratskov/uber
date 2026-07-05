package com.example.payment_service.model.event;

import com.example.payment_service.model.entity.PaymentEntity;
import com.example.payment_service.model.entity.PaymentTransactionEntity;
import lombok.Builder;

import java.util.UUID;

@Builder
public record PaymentCaptureFailedEvent(
        UUID rideId,
        UUID paymentId,
        UUID transactionId,
        String errorMessage
) {
    public static PaymentCaptureFailedEvent createEvent(
            PaymentTransactionEntity transaction,
            PaymentEntity payment) {

        return PaymentCaptureFailedEvent.builder()
                .rideId(payment.getRideId())
                .paymentId(payment.getId())
                .transactionId(transaction.getId())
                .errorMessage(transaction.getErrorMessage())
                .build();
    }
}
