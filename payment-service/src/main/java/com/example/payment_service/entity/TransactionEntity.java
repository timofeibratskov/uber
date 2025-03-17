package com.example.payment_service.entity;

import com.example.payment_service.enums.TransactionStatus;
import com.example.payment_service.enums.TransactionType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("transaction")
public class TransactionEntity {
    @Id
    private Long id;

    @Column("sender_id")
    private Long senderId;

    @Column("recipient_id")
    private Long recipientId;

    private BigDecimal amount;

    @Column("transaction_date")
    private LocalDateTime transactionDate;

    @Column("transaction_type")
    private TransactionType transactionType;

    @Column("ride_id")
    private String rideId;

    @Column
    private TransactionStatus status;
}