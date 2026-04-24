package com.example.payment_service.domain.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PaymentTransaction {
    private final UUID id;
    private final UUID rideId;
    private final UUID userId;
    private final Money amount;
    private TransactionStatus status;

    public PaymentTransaction(UUID rideId, UUID userId, Money amount) {
        this.id = UUID.randomUUID();
        this.rideId = rideId;
        this.userId = userId;
        this.amount = amount;
        this.status = TransactionStatus.CREATED;
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