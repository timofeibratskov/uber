package com.example.payment_service.service;

import com.example.payment_service.model.entity.PaymentEntity;
import com.example.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentEntity save(PaymentEntity paymentEntity) {
        return paymentRepository.save(paymentEntity);
    }

    public Optional<PaymentEntity> findByRideId(UUID rideId) {
        return paymentRepository.findByRideId(rideId);
    }
}
