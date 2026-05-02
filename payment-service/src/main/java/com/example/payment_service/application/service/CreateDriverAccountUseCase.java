package com.example.payment_service.application.service;

import com.example.payment_service.application.dto.CreateDriverAccountRequest;
import com.example.payment_service.domain.gateway.PaymentGateway;
import com.example.payment_service.domain.model.DriverAccount;
import com.example.payment_service.domain.repository.DriverAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateDriverAccountUseCase {
    private final DriverAccountRepository driverAccountRepository;
    private final PaymentGateway paymentGateway;

    @Transactional
    public void execute(CreateDriverAccountRequest request) {
        var accountId = paymentGateway.createAccount(request.email());

        var stripeAccount = DriverAccount.builder()
                .driverId(request.driverId())
                .accountId(accountId)
                .build();

        driverAccountRepository.insert(stripeAccount);
    }
}
