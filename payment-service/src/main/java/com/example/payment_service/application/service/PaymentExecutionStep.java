package com.example.payment_service.application.service;

import com.example.payment_service.domain.model.EventType;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentTransaction;
import com.example.payment_service.domain.model.TopicType;
import com.example.payment_service.domain.repository.PaymentTransactionRepository;
import com.example.payment_service.domain.service.PaymentDomainService;
import com.example.payment_service.infrastructure.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentExecutionStep {
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentDomainService domainService;
    private final OutboxService outboxService;

    @Transactional
    public void executeSuccessfulStep(PaymentTransaction transaction, PaymentMethod method) {

        domainService.process(transaction, method);

        outboxService.saveEvent(
                transaction.getRideId(),
                EventType.PAYMENT_COMPLETED,
                TopicType.PAYMENT
        );

        transactionRepository.insert(transaction);
    }
}
