package com.example.payment_service.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentMethod {
    private final UUID id;
    private final UUID userId;
    private final PaymentType type;
    private final String externalToken;
    private boolean isDeleted;

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
        this.isDeleted = false;
    }

    public static PaymentMethod restore(UUID id, UUID userId, PaymentType type, String externalToken, boolean isDeleted) {
        return PaymentMethod.builder()
                .id(id)
                .userId(userId)
                .type(type)
                .externalToken(externalToken)
                .isDeleted(isDeleted)
                .build();
    }

    public static PaymentMethod createCashMethod(UUID userId) {
        return new PaymentMethod(UUID.randomUUID(), userId, PaymentType.CASH, null);
    }

    public static PaymentMethod createCardMethod(UUID userId, String token) {
        return new PaymentMethod(UUID.randomUUID(), userId, PaymentType.CARD, token);
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }

    public void unmarkAsDeleted() {
        this.isDeleted = false;
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