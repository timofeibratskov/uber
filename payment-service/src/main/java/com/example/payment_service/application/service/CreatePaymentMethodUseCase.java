package com.example.payment_service.application.service;

import com.example.payment_service.application.dto.CreatePaymentMethodRequest;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.domain.model.PaymentType;
import com.example.payment_service.domain.repository.PaymentMethodRepository;
import com.example.payment_service.domain.service.PaymentMethodValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePaymentMethodUseCase {
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodValidator validator;

    @Transactional
    public void execute(CreatePaymentMethodRequest request) {
        var existingMethods = paymentMethodRepository.findAllByUserId(request.userId());

        validator.validate(existingMethods, request.paymentType(), request.externalToken());

        var deletedMethod = existingMethods
                .stream()
                .filter(paymentMethod ->
                        paymentMethod.getExternalToken().equals(request.externalToken()))
                .findFirst();

        if (deletedMethod.isPresent()) {
            log.info("Payment method with flag=isDeleted already exists in db: {}", deletedMethod.get().getId());
            var method = deletedMethod.get();
            method.unmarkAsDeleted();
            paymentMethodRepository.update(method);
            log.info("Payment method with flag=isDeleted was updated: {}", method.getId());
        } else {
            PaymentMethod method = request.paymentType().equals(PaymentType.CARD) ?
                    PaymentMethod.createCardMethod(request.userId(), request.externalToken()) :
                    PaymentMethod.createCashMethod(request.userId());

            paymentMethodRepository.insert(method);
            log.info("new payment method is created: {}", method.getId());
        }
    }
}