package com.example.payment_service.model.entity;

import com.example.payment_service.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table(name = "payments_table")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEntity implements Persistable<UUID> {
    @Id
    private UUID id;
    private UUID rideId;
    private UUID paymentMethodId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String intentId;


    public static PaymentEntity createPaymentEntity(
            UUID rideId,
            UUID paymentMethodId,
            String intentId,
            BigDecimal amount,
            String currency
    ) {
        return PaymentEntity.builder()
                .id(UUID.randomUUID())
                .rideId(rideId)
                .paymentMethodId(paymentMethodId)
                .amount(amount)
                .currency(currency)
                .intentId(intentId)
                .status(PaymentStatus.PENDING)
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
