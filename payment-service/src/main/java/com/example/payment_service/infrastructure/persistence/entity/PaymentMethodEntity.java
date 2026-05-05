package com.example.payment_service.infrastructure.persistence.entity;

import com.example.payment_service.domain.model.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "payment_methods_table")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodEntity {
    @Id
    private UUID id;
    private UUID userId;
    private PaymentType type;
    private String externalToken;
    boolean isDeleted;
}