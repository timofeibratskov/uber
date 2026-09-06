package com.example.payment_service.service.handler;

import com.example.payment_service.exception.PaymentMethodNotFoundException;
import com.example.payment_service.model.event.RideCreatedEvent;
import com.example.payment_service.service.PaymentMethodService;
import com.example.payment_service.service.provider.PaymentProviderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthorizePaymentHandler
        implements PaymentHandler<RideCreatedEvent> {
    private final PaymentMethodService paymentMethodService;
    private final PaymentProviderFactory paymentProviderFactory;

    @Override
    @Transactional
    public void handle(RideCreatedEvent event) {
        var paymentMethod = paymentMethodService.findById(event.paymentMethodId())
                .orElseThrow(() -> new PaymentMethodNotFoundException("Payment Method Not Found"));

        if (!paymentMethod.getUserId().equals(event.passengerId()))
            throw new IllegalStateException("Payment method does not belong to the user");

        var provider = paymentProviderFactory.getProvider(paymentMethod.getType());

        provider.authorize(
                paymentMethod,
                event.amount(),
                event.currency(),
                event.rideId(),
                event.seats(),
                event.startPoint());
    }

    @Override
    public Class<RideCreatedEvent> getEventType() {
        return RideCreatedEvent.class;
    }
}