package com.example.passenger_service.dto;

import lombok.Builder;

@Builder
public record PassengerDto(
        Long id,
        String name,
        String gmail,
        String password,
        String phoneNumber,
        Long cardId,
        Float rating
) {

}