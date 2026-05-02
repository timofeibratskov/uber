package com.example.payment_service.infrastructure.config;

import com.example.payment_service.domain.service.PaymentDomainService;
import com.example.payment_service.domain.service.PaymentMethodValidator;
import com.example.payment_service.domain.service.PaymentStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DomainConfig {
    @Bean
    public PaymentDomainService paymentDomainService(List<PaymentStrategy> strategies) {
        return new PaymentDomainService(strategies);
    }

    @Bean
    PaymentMethodValidator paymentMethodValidator() {
        return new PaymentMethodValidator();
    }
}
