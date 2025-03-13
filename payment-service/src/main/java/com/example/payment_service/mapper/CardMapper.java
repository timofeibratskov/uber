package com.example.payment_service.mapper;

import com.example.payment_service.dto.CardRequestDto;
import com.example.payment_service.dto.CardResponseDto;
import com.example.payment_service.entity.CardEntity;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {
    public CardEntity toEntity(CardRequestDto dto) {
        return CardEntity.builder()
                .cardNumber(dto.cardNumber())
                .balance(dto.balance())
                .password(dto.password())
                .build();
    }
    public CardResponseDto toDto(CardEntity entity){
        return CardResponseDto.builder()
                .id(entity.getId())
                .cardNumber(entity.getCardNumber())
                .balance(entity.getBalance())
                .password(entity.getPassword())
                .ownerId(entity.getOwnerId())
                .role(entity.getRole())
                .build();
    }
}
