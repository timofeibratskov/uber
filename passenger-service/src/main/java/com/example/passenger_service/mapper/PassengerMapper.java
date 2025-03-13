package com.example.passenger_service.mapper;

import com.example.passenger_service.dto.PassengerDto;
import com.example.passenger_service.dto.PassengerRequest;
import com.example.passenger_service.entity.PassengerEntity;
import org.springframework.stereotype.Component;

@Component
public class PassengerMapper {
    public PassengerEntity toEntity(PassengerRequest request) {
        return PassengerEntity.builder()
                .name(request.name())
                .gmail(request.gmail())
                .password(request.password())
                .phoneNumber(request.phoneNumber())
                .rating(0.0f)
                .build();
    }


    public PassengerDto toDto(PassengerEntity entity) {
        return PassengerDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .gmail(entity.getGmail())
                .password(entity.getPassword())
                .phoneNumber(entity.getPhoneNumber())
                .rating(entity.getRating())
                .build();
    }
}