package com.example.payment_service.entity;

import com.example.payment_service.enums.TransactionStatus;
import com.example.payment_service.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.springframework.data.annotation.Id;
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
    private Long senderCardId;
    private Long recipientCardId;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private TransactionType transactionType;
    private String rideId;
    private TransactionStatus status;

}
