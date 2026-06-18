package com.example.payment_service.model.entity;

import com.example.payment_service.model.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "payment_methods_table")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodEntity implements Persistable<UUID> {
    @Id
    private UUID id;
    private UUID userId;
    private PaymentType type;
    private String externalToken;
    private boolean isDeleted;

    public static PaymentMethodEntity toNewCashEntity(UUID userId) {
        return PaymentMethodEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(PaymentType.CASH)
                .isDeleted(false)
                .isNew(true)
                .build();
    }

    public static PaymentMethodEntity toNewCardEntity(UUID userId, String externalToken) {
        return PaymentMethodEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(PaymentType.CARD)
                .externalToken(externalToken)
                .isDeleted(false)
                .isNew(true)
                .build();
    }

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }
}