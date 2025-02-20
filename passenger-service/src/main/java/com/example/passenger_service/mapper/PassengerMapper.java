package com.example.passenger_service.mapper;

import com.example.passenger_service.dto.PassengerDto;
import com.example.passenger_service.dto.RegisterPassengerDto;
import com.example.passenger_service.entity.PassengerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PassengerMapper {
    PassengerDto toPassengerDto(PassengerEntity passenger);

    PassengerEntity toPassenger(RegisterPassengerDto passengerDto);

    RegisterPassengerDto toRegisterPassengerDto(PassengerEntity passenger);

}