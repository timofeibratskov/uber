package com.example.payment_service.service.provider;

import com.example.payment_service.model.enums.PaymentType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PaymentProviderFactory {
    private final Map<PaymentType, PaymentProvider> providers;

    public PaymentProviderFactory(List<PaymentProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(PaymentProvider::getType, p -> p));
    }

    public PaymentProvider getProvider(PaymentType type) {
        return Optional.ofNullable(providers.get(type))
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported payment method type: " + type));
    }
}
