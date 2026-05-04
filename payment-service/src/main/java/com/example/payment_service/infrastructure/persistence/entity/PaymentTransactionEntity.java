package com.example.payment_service.infrastructure.persistence.entity;

import com.example.payment_service.domain.model.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table(name = "payment_transactions_table")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransactionEntity {
    @Id
    private UUID id;
    private UUID rideId;
    private UUID passengerId;
    private UUID driverId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
}