package com.example.ride_service.mapper;

import com.example.ride_service.model.cache.RideEstimateCache;
import com.example.ride_service.model.dto.RideCreateResponseDto;
import com.example.ride_service.model.dto.RideEstimateResponseDto;
import com.example.ride_service.model.entity.RideEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RideMapper {
    @Mapping(target = "expiration", constant = "600L")
    RideEstimateCache toCache(RideEstimateResponseDto estimate, UUID passengerId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "finalAmount", source = "price")
    @Mapping(target = "status", constant = "CREATED")
    RideEntity toEntity(RideEstimateCache cache);

    @Mapping(target = "price", source = "finalAmount")
    @Mapping(target = "statusDescription", source = "status.description")
    RideCreateResponseDto toRideCreateResponseDto(RideEntity rideEntity);
}