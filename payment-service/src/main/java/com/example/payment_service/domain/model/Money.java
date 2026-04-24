package com.example.payment_service.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Currency;

@Builder
public record Money(
        BigDecimal amount,
        Currency currency
) {
    public Money {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Amount cannot be zero");
        }
    }

    public Money add(Money other) {
        checkCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        checkCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    private void checkCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("different currency");
        }
    }
}