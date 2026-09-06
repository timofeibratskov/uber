package com.example.payment_service.service.handler;

import com.example.payment_service.exception.PaymentMethodNotFoundException;
import com.example.payment_service.exception.PaymentNotFoundException;
import com.example.payment_service.model.event.RideCanceledEvent;
import com.example.payment_service.service.PaymentMethodService;
import com.example.payment_service.service.PaymentService;
import com.example.payment_service.service.provider.PaymentProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReleasePaymentHandler
        implements PaymentHandler<RideCanceledEvent> {
    private final PaymentService paymentService;
    private final PaymentMethodService methodService;
    private final PaymentProviderFactory paymentProviderFactory;

    @Override
    @Transactional
    public void handle(RideCanceledEvent event) {
        var payment = paymentService.findByRideId(event.rideId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        var method = methodService.findById(payment.getPaymentMethodId())//join in db?
                .orElseThrow(() -> new PaymentMethodNotFoundException("payment method not found"));

        var provider = paymentProviderFactory.getProvider(method.getType());
        provider.release(payment);
    }

    @Override
    public Class<RideCanceledEvent> getEventType() {
        return RideCanceledEvent.class;
    }
}
