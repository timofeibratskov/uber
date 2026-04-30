package com.example.payment_service.application.service;

import com.example.payment_service.application.dto.CreatePaymentMethodRequest;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentType;
import com.example.payment_service.domain.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePaymentMethodUseCase {
    private final PaymentMethodRepository paymentMethodRepository;

    @Transactional
    public void execute(CreatePaymentMethodRequest request) {
        PaymentMethod method = request.paymentType().equals(PaymentType.CARD) ?
                PaymentMethod.createCardMethod(request.userId(), request.externalToken()) :
                PaymentMethod.createCashMethod(request.userId());

        paymentMethodRepository.insert(method);
    }
}