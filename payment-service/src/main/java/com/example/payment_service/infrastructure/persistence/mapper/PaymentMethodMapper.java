package com.example.payment_service.infrastructure.persistence.mapper;

import com.example.payment_service.application.dto.UserPaymentMethodResponse;
import com.example.payment_service.domain.model.PaymentMethod;
import com.example.payment_service.infrastructure.persistence.entity.PaymentMethodEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodMapper {
    public PaymentMethodEntity toEntity(PaymentMethod domain) {
        return PaymentMethodEntity.builder()
                .id(domain.getId())
                .type(domain.getType())
                .externalToken(domain.getExternalToken())
                .userId(domain.getUserId())
                .isDefault(domain.isDefault())
                .build();
    }

    public PaymentMethod toDomain(PaymentMethodEntity entity) {
        return PaymentMethod.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getExternalToken(),
                entity.isDefault()
        );
    }

    public UserPaymentMethodResponse toResponse(PaymentMethod domain) {
        return UserPaymentMethodResponse.builder()
                .id(domain.getId())
                .type(domain.getType())
                .isDefault(domain.isDefault())
                .build();
    }
}