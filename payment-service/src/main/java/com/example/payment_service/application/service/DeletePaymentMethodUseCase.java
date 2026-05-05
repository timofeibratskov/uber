package com.example.payment_service.application.service;

import com.example.payment_service.domain.exception.PaymentMethodNotFoundException;
import com.example.payment_service.domain.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class DeletePaymentMethodUseCase {
    private final PaymentMethodRepository paymentMethodRepository;

    @Transactional
    public void execute(UUID id) {
        var paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentMethodNotFoundException("payment method not found"));

        paymentMethod.markAsDeleted();

        paymentMethodRepository.update(paymentMethod);
    }
}
