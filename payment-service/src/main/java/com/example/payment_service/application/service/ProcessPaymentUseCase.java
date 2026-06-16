package com.example.payment_service.application.service;

import com.example.payment_service.application.dto.CreatePaymentRequest;
import com.example.payment_service.domain.exception.PaymentMethodNotFoundException;
import com.example.payment_service.domain.model.Money;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentTransaction;
import com.example.payment_service.domain.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {
    private final PaymentMethodRepository methodRepository;
    private final PaymentExecutionStep executionStep;
    private final PaymentFailedStep failureStep;

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
            executionStep.executeSuccessfulStep(transaction, method);
        } catch (Exception e) {
            failureStep.executeFailedStep(transaction);

            throw e;
        }
    }
}