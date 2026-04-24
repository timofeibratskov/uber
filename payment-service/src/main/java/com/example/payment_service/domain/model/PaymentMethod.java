package com.example.payment_service.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class PaymentMethod {
    private final UUID id;
    private final UUID userId;
    private final PaymentType type;
    private final String externalToken;
    private boolean isDefault;

    public PaymentMethod(UUID id, UUID userId, PaymentType type, String externalToken) {
        if (id == null || userId == null || type == null) {
            throw new IllegalArgumentException("Required fields cannot be empty or null");
        }

        if (type == PaymentType.CARD && (externalToken == null || externalToken.isBlank())) {
            throw new IllegalArgumentException("for payment external token must not be null or blank");
        }

        this.id = id;
        this.userId = userId;
        this.type = type;
        this.externalToken = externalToken;
        this.isDefault = false;
    }

    public static PaymentMethod createCashMethod(UUID userId) {
        return new PaymentMethod(UUID.randomUUID(), userId, PaymentType.CASH, null);
    }

    public static PaymentMethod createCardMethod(UUID userId, String token) {
        return new PaymentMethod(UUID.randomUUID(), userId, PaymentType.CARD, token);
    }

    public void markAsDefault() {
        this.isDefault = true;
    }

    public void unmarkAsDefault() {
        this.isDefault = false;
    }

    public boolean isAutomated() {
        return type == PaymentType.CARD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentMethod that = (PaymentMethod) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}