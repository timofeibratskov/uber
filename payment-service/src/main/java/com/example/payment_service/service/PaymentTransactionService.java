package com.example.payment_service.service;

import com.example.payment_service.model.entity.PaymentTransactionEntity;
import com.example.payment_service.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransactionService {
    private final PaymentTransactionRepository repository;

    @Transactional
    public PaymentTransactionEntity save(PaymentTransactionEntity entity) {
        return repository.save(entity);
    }
}
