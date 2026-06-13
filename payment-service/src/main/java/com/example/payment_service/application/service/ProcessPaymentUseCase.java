package com.example.payment_service.application.service;

import com.example.payment_service.application.dto.CreatePaymentRequest;
import com.example.payment_service.domain.exception.PaymentMethodNotFoundException;
import com.example.payment_service.domain.model.EventType;
import com.example.payment_service.domain.model.Money;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentTransaction;
import com.example.payment_service.domain.model.TopicType;
import com.example.payment_service.domain.repository.PaymentMethodRepository;
import com.example.payment_service.domain.repository.PaymentTransactionRepository;
import com.example.payment_service.domain.service.PaymentDomainService;
import com.example.payment_service.infrastructure.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {
    private final PaymentMethodRepository methodRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentDomainService domainService;
    private final OutboxService outboxService;

    @Transactional
    public void execute(CreatePaymentRequest request) {
        PaymentMethod method = methodRepository.findById(request.paymentMethodId())
                .orElseThrow(() ->
                        new PaymentMethodNotFoundException("payment not found with id: " + request.paymentMethodId()));

        PaymentTransaction transaction = new PaymentTransaction(
                request.rideId(),
                request.passengerId(),
                request.driverId(),
                Money.of(request.amount(), request.currency())
        );

        try {
            domainService.process(transaction, method);

            outboxService.saveEvent(
                    transaction.getRideId(),
                    EventType.PAYMENT_COMPLETED,
                    TopicType.PAYMENT
            );

        } finally {
            transactionRepository.insert(transaction);
        }
    }
}