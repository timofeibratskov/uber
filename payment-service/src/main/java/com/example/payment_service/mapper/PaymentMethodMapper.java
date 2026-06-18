package com.example.payment_service.mapper;

import com.example.payment_service.model.dto.UserPaymentMethodResponse;
import com.example.payment_service.model.entity.PaymentMethodEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodMapper {
       public UserPaymentMethodResponse toResponse(PaymentMethodEntity entity) {
        return UserPaymentMethodResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .build();
    }
}