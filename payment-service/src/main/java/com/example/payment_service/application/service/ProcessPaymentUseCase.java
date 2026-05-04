package com.example.payment_service.application.service;

import com.example.payment_service.application.dto.CreatePaymentRequest;
import com.example.payment_service.domain.exception.PaymentNotFoundException;
import com.example.payment_service.domain.model.Money;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentTransaction;
import com.example.payment_service.domain.repository.PaymentMethodRepository;
import com.example.payment_service.domain.repository.PaymentTransactionRepository;
import com.example.payment_service.domain.service.PaymentDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {
    private final PaymentMethodRepository methodRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentDomainService domainService;

    @Transactional
    public void execute(CreatePaymentRequest request) {
        PaymentMethod method = methodRepository.findById(request.paymentMethodId())
                .orElseThrow(() ->
                        new PaymentNotFoundException("payment not found with id: " + request.paymentMethodId()));

        PaymentTransaction transaction = new PaymentTransaction(
                request.rideId(),
                request.passengerId(),
                request.driverId(),
                Money.of(request.amount(), request.currency())
        );

        try {
            domainService.process(transaction, method);
        } finally {
            transactionRepository.insert(transaction);
        }
    }
}