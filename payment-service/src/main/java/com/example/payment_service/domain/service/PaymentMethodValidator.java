package com.example.payment_service.domain.service;

import com.example.payment_service.domain.exception.PaymentMethodAlreadyExistsException;
import com.example.payment_service.domain.exception.PaymentMethodLimitExceededException;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentType;

import java.util.List;
import java.util.Objects;

public class PaymentMethodValidator {
    private static final int MAX_CARD_LIMIT = 3;

    public void validate(List<PaymentMethod> existingMethods, PaymentType newType, String newToken) {
        if (newType == PaymentType.CARD) {
            validateCardLimit(existingMethods);
            validateCardUniqueness(existingMethods, newToken);
        } else if (newType == PaymentType.CASH) {
            validateCashUniqueness(existingMethods);
        }
    }

    private void validateCardLimit(List<PaymentMethod> methods) {
        long cardCount = methods.stream()
                .filter(m -> m.getType() == PaymentType.CARD)
                .count();

        if (cardCount >= MAX_CARD_LIMIT) {
            throw new PaymentMethodLimitExceededException("You have reached the maximum limit of " + MAX_CARD_LIMIT + " cards");
        }
    }

    private void validateCardUniqueness(List<PaymentMethod> methods, String newToken) {
        boolean cardExists = methods.stream()
                .filter(m -> m.getType() == PaymentType.CARD)
                .anyMatch(m ->
                        Objects.equals(m.getExternalToken(), newToken) &&
                                !m.isDeleted()
                );

        if (cardExists) {
            throw new PaymentMethodAlreadyExistsException("This card is already linked to your account");
        }
    }

    private void validateCashUniqueness(List<PaymentMethod> methods) {
        boolean hasCash = methods.stream()
                .anyMatch(m -> m.getType() == PaymentType.CASH);

        if (hasCash) {
            throw new PaymentMethodAlreadyExistsException("Cash payment method is already enabled");
        }
    }
}