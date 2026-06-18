package com.example.payment_service.model.entity;

import com.example.payment_service.model.enums.TransactionStatus;
import com.example.payment_service.model.dto.GatewayAuthorizationResult;
import com.example.payment_service.model.enums.TransactionType;
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
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "payment_transactions_table")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransactionEntity implements Persistable<UUID> {
    @Id
    private UUID id;
    private UUID paymentId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private TransactionType type;
    private String chargeId;
    private String errorMessage;
    private LocalDateTime createdAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    public static PaymentTransactionEntity createForAuthorize(
            PaymentEntity payment,
            GatewayAuthorizationResult result) {

        return PaymentTransactionEntity.builder()
                .id(UUID.randomUUID())
                .paymentId(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(result.isSuccess() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED)
                .type(TransactionType.AUTHORIZE)
                .chargeId(result.chargeId())
                .errorMessage(result.errorMessage())
                .createdAt(LocalDateTime.now())
                .isNew(true)
                .build();
    }

    public static PaymentTransactionEntity createForCapture(
            PaymentEntity payment,
            GatewayAuthorizationResult result) {

        return PaymentTransactionEntity.builder()
                .id(UUID.randomUUID())
                .paymentId(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(result.isSuccess() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED)
                .type(TransactionType.CAPTURE)
                .chargeId(result.chargeId())
                .errorMessage(result.errorMessage())
                .createdAt(LocalDateTime.now())
                .isNew(true)
                .build();
    }

    public static PaymentTransactionEntity createForRelease(
            PaymentEntity payment,
            GatewayAuthorizationResult result) {

        return PaymentTransactionEntity.builder()
                .id(UUID.randomUUID())
                .paymentId(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(result.isSuccess() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED)
                .type(TransactionType.RELEASE)
                .chargeId(result.chargeId())
                .errorMessage(result.errorMessage())
                .createdAt(LocalDateTime.now())
                .isNew(true)
                .build();
    }
}