package com.example.passenger_service.mapper;

import com.example.passenger_service.model.dto.CompleteProfileRequestDto;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.entity.PassengerEntity;
import com.example.passenger_service.model.events.UserRegisteredEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PassengerMapper {

    void updateEntity(
            @MappingTarget PassengerEntity passengerEntity,
            CompleteProfileRequestDto registerPassengerDto);

    @Mapping(source = "userId", target = "id")
    PassengerEntity toEntity(UserRegisteredEvent event);

    PassengerResponseDto toResponseDto(PassengerEntity passengerEntity, BigDecimal rating);
}