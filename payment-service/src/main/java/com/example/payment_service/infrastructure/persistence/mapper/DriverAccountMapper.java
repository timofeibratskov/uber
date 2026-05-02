package com.example.payment_service.infrastructure.persistence.mapper;


import com.example.payment_service.domain.model.DriverAccount;
import com.example.payment_service.infrastructure.persistence.entity.DriverAccountEntity;
import org.springframework.stereotype.Component;

@Component
public class DriverAccountMapper {
    public DriverAccountEntity toEntity(DriverAccount domain) {
        return DriverAccountEntity.builder()
                .driverId(domain.driverId())
                .accountId(domain.accountId())
                .build();
    }

    public DriverAccount toDomain(DriverAccountEntity entity) {
        return DriverAccount.builder()
                .driverId(entity.getDriverId())
                .accountId(entity.getAccountId())
                .build();
    }
}
