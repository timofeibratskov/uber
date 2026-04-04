package com.example.passenger_service.mapper;

import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.RegisterPassengerDto;
import com.example.passenger_service.model.entity.PassengerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PassengerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "rating", ignore = true)
    PassengerEntity toEntity(RegisterPassengerDto registerPassengerDto);

    PassengerResponseDto toResponseDto(PassengerEntity passengerEntity);
}