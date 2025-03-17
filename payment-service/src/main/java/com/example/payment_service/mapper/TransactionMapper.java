package com.example.payment_service.mapper;

import com.example.payment_service.dto.TransactionRequestDto;
import com.example.payment_service.entity.TransactionEntity;
import com.example.payment_service.enums.TransactionStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransactionMapper {
    public TransactionEntity toEntity(TransactionRequestDto dto){
        return TransactionEntity.builder()
                .senderId(dto.senderId())
                .recipientId(dto.recipientId())
                .amount(dto.amount())
                .transactionDate(LocalDateTime.now())
                .rideId(dto.rideId())
                .status(TransactionStatus.PENDING)
                .build();
    }
}
