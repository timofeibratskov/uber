package com.example.payment_service.service;

import com.example.payment_service.exception.DriverAccountAlreadyExistsException;
import com.example.payment_service.model.dto.CreateDriverAccountRequest;
import com.example.payment_service.model.entity.DriverAccountEntity;
import com.example.payment_service.repository.DriverAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverAccountService {
    private final DriverAccountRepository driverAccountRepository;
    private final StripeGatewayService stripeGatewayService;

    @Transactional
    public void create(CreateDriverAccountRequest request) {
        if (driverAccountRepository.findByDriverId(request.driverId()).isPresent()) {
            throw new DriverAccountAlreadyExistsException("Driver Account already exists");
        }

        var accountId = stripeGatewayService.createAccount(request.email());

        var stripeAccount = DriverAccountEntity.builder()
                .driverId(request.driverId())
                .accountId(accountId)
                .isNew(true)
                .build();

        driverAccountRepository.save(stripeAccount);
    }

    public Optional<DriverAccountEntity> findByDriverId(UUID driverId) {
        return driverAccountRepository.findByDriverId(driverId);
    }
}
