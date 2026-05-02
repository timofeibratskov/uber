package com.example.payment_service.application.service;

import com.example.payment_service.application.dto.UserPaymentMethodResponse;
import com.example.payment_service.domain.repository.PaymentMethodRepository;
import com.example.payment_service.infrastructure.persistence.mapper.PaymentMethodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPaymentMethodsUseCase {
    private final PaymentMethodMapper mapper;
    private final PaymentMethodRepository repository;

    @Transactional(readOnly = true)
    public List<UserPaymentMethodResponse> findUsersPaymentMethods(UUID id) {
        return repository.findAllByUserId(id)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
