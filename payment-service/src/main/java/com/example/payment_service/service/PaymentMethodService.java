package com.example.payment_service.service;

import com.example.payment_service.exception.PaymentMethodNotFoundException;
import com.example.payment_service.mapper.PaymentMethodMapper;
import com.example.payment_service.model.dto.CreatePaymentMethodRequest;
import com.example.payment_service.model.dto.UserPaymentMethodResponse;
import com.example.payment_service.model.entity.PaymentMethodEntity;
import com.example.payment_service.model.enums.PaymentType;
import com.example.payment_service.repository.PaymentMethodRepository;
import com.example.payment_service.util.PaymentMethodValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMethodService {
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodValidator validator;
    private final PaymentMethodMapper mapper;

    @Transactional
    public void create(CreatePaymentMethodRequest request) {
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
            method.setDeleted(false);
            method.setNew(false);
            paymentMethodRepository.save(method);

            log.info("Payment method with flag=isDeleted was updated: {}", method.getId());
        } else {
            var method = request.paymentType().equals(PaymentType.CARD) ?
                    PaymentMethodEntity.toNewCardEntity(request.userId(), request.externalToken()) :
                    PaymentMethodEntity.toNewCashEntity(request.userId());

            paymentMethodRepository.save(method);
            log.info("new payment method is created: {}", method.getId());
        }
    }


    public List<UserPaymentMethodResponse> findAllByUserId(UUID userId) {
        return paymentMethodRepository.findAllByUserIdAndIsDeletedFalse(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public Optional<PaymentMethodEntity> findById(UUID id) {
        return paymentMethodRepository.findById(id);
    }

    @Transactional
    public void deleteById(UUID id) {
        var paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentMethodNotFoundException("payment method not found"));

        paymentMethod.setDeleted(true);
        paymentMethod.setNew(false);

        paymentMethodRepository.save(paymentMethod);
    }
}
