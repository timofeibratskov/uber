package com.example.payment_service.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentTransaction {
    private final UUID id;
    private final UUID rideId;
    private final UUID passengerId;
    private final UUID driverId;
    private final Money amount;
    private TransactionStatus status;

    public PaymentTransaction(UUID rideId, UUID passengerId,UUID driverId, Money amount) {
        this.id = UUID.randomUUID();
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.amount = amount;
        this.status = TransactionStatus.CREATED;
    }

    public static PaymentTransaction restore(UUID id, UUID rideId, UUID passengerId,UUID driverId, Money amount, TransactionStatus status) {
        return PaymentTransaction.builder()
                .id(id)
                .rideId(rideId)
                .passengerId(passengerId)
                .driverId(driverId)
                .amount(amount)
                .status(status)
                .build();
    }

    public void startProcessing() {
        if (this.status != TransactionStatus.CREATED) {
            throw new IllegalStateException("Payment can only be started for transactions in CREATED status");
        }
        this.status = TransactionStatus.PROCESSING;
    }

    public void complete() {
        if (this.status != TransactionStatus.PROCESSING) {
            throw new IllegalStateException("Only transactions in PROCESSING status can be completed");
        }
        this.status = TransactionStatus.SUCCESS;
    }

    public void fail() {
        if (this.status == TransactionStatus.SUCCESS || this.status == TransactionStatus.REFUNDED) {
            throw new IllegalStateException("Cannot fail a transaction that has already been finalized");
        }
        this.status = TransactionStatus.FAILED;
    }

    public void refund() {
        if (this.status != TransactionStatus.SUCCESS) {
            throw new IllegalStateException("Only successful transactions can be refunded");
        }
        this.status = TransactionStatus.REFUNDED;
    }
}