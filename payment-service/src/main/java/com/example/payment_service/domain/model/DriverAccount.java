package com.example.payment_service.domain.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DriverAccount(
        UUID driverId,
        String accountId
) {
    public DriverAccount {
        if (accountId == null) {
            throw new IllegalArgumentException("Account id cannot be null");
        }
        if (driverId == null) {
            throw new IllegalArgumentException("Driver id cannot be null");
        }

        if (!accountId.startsWith("acct_")) {
            throw new IllegalArgumentException("Account id must start with 'acct_'");
        }
    }
}
