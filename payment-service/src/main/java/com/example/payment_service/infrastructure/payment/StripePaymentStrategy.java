package com.example.payment_service.infrastructure.payment;

import com.example.payment_service.domain.exception.PaymentDeclinedException;
import com.example.payment_service.domain.exception.StripeServiceException;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentTransaction;
import com.example.payment_service.domain.model.PaymentType;
import com.example.payment_service.domain.service.PaymentStrategy;
import com.example.payment_service.domain.util.MinorUntilConverter;
import com.stripe.StripeClient;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripePaymentStrategy implements PaymentStrategy {
    private final StripeClient stripeClient;

    @Override
    public void execute(PaymentTransaction transaction, PaymentMethod method) {
        try {
            long amountInMinorUnits = MinorUntilConverter.convert(
                    transaction.getAmount().amount(),
                    transaction.getAmount().currency().getCurrencyCode()
            );

            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams.builder()
                            .setAmount(amountInMinorUnits)
                            .setCurrency(transaction.getAmount().currency().getCurrencyCode())
                            .setConfirm(true)
                            .setPaymentMethod(method.getExternalToken())
                            .addPaymentMethodType("card")
                            .build();

            stripeClient.v1().paymentIntents().create(params);
            log.info("Successfully executed payment method id: {} for transaction id: {}", method.getId(), transaction.getId());
        } catch (CardException e) {
            log.warn("Payment declined for transaction {}: {}", transaction.getId(), e.getMessage());
            throw new PaymentDeclinedException("card declined: " + e.getMessage());

        } catch (StripeException e) {
            log.error("Stripe API error for transaction {}: {}", transaction.getId(), e.getMessage());
            throw new StripeServiceException("External payment service unavailable");
        } catch (Exception e) {
            log.error("Unexpected error for transaction {}: {}", transaction.getId(), e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PaymentType getType() {
        return PaymentType.CARD;
    }
}