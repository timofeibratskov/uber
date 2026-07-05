package com.example.payment_service.model.event;

import com.example.payment_service.model.entity.PaymentEntity;
import com.example.payment_service.model.entity.PaymentTransactionEntity;
import lombok.Builder;

import java.util.UUID;

@Builder
public record PaymentCapturedEvent(
        UUID rideId,
        UUID paymentId,
        UUID transactionId
) {
    public static PaymentCapturedEvent createEvent(
            PaymentTransactionEntity transaction,
            PaymentEntity payment) {

        return PaymentCapturedEvent.builder()
                .rideId(payment.getRideId())
                .paymentId(payment.getId())
                .transactionId(transaction.getId())
                .build();
    }
}
