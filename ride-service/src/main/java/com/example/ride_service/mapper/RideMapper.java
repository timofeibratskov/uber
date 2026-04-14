package com.example.ride_service.mapper;

import com.example.ride_service.model.cache.RideEstimateCache;
import com.example.ride_service.model.dto.RideAcceptedRequestDto;
import com.example.ride_service.model.dto.RideAcceptedResponseDto;
import com.example.ride_service.model.dto.RideCancelRequestDto;
import com.example.ride_service.model.dto.RideCreateResponseDto;
import com.example.ride_service.model.dto.RideEndResponseDto;
import com.example.ride_service.model.dto.RideEstimateResponseDto;
import com.example.ride_service.model.entity.RideEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.Duration;
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

    @Mapping(target = "statusDescription", source = "status.description")
    RideAcceptedResponseDto toRideAcceptedResponseDto(RideEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "seats", ignore = true)
    void updateRideFromDto(RideAcceptedRequestDto dto, @MappingTarget RideEntity entity);

    @Mapping(target = "cancelAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "cancelReasonComment", source = "comment")
    void cancelRideFromDto(RideCancelRequestDto dto, @MappingTarget RideEntity entity);

    @Mapping(target = "durationMinutes", source = "rideEntity")
    RideEndResponseDto toRideEndResponseDto(RideEntity rideEntity);

    default Long mapDuration(RideEntity entity) {
        return (entity.getStartAt() == null || entity.getEndAt() == null) ?
                null :
                Duration.between(entity.getStartAt(), entity.getEndAt()).toMinutes();
    }
}