package com.example.payment_service.application.service;

import com.example.payment_service.domain.model.EventType;
import com.example.payment_service.domain.model.PaymentTransaction;
import com.example.payment_service.domain.model.TopicType;
import com.example.payment_service.domain.repository.PaymentTransactionRepository;
import com.example.payment_service.infrastructure.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentFailedStep {
    private final PaymentTransactionRepository transactionRepository;
    private final OutboxService outboxService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeFailedStep(PaymentTransaction transaction) {
        outboxService.saveEvent(
                transaction.getRideId(),
                EventType.PAYMENT_FAILED,
                TopicType.PAYMENT
        );

        transactionRepository.insert(transaction);
    }
}
