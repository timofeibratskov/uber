package com.example.ride_service.mapper;

import com.example.ride_service.model.cache.RideEstimateCache;
import com.example.ride_service.model.dto.DriverResponseDto;
import com.example.ride_service.model.dto.RideCancelRequestDto;
import com.example.ride_service.model.dto.RideCreateResponseDto;
import com.example.ride_service.model.dto.RideEndResponseDto;
import com.example.ride_service.model.dto.RideEstimateRequestDto;
import com.example.ride_service.model.dto.RideEstimateResponseDto;
import com.example.ride_service.model.dto.RideFullResponseDto;
import com.example.ride_service.model.entity.RideEntity;
import com.example.ride_service.model.event.DriverAssignedEvent;
import com.example.ride_service.model.event.NoDriversEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.Duration;

@Mapper(componentModel = "spring")
public interface RideMapper {
    @Mapping(target = "expiration", constant = "600L")
    RideEstimateCache toCache(RideEstimateResponseDto estimate, RideEstimateRequestDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "finalAmount", source = "price")
    @Mapping(target = "status", ignore = true)
    RideEntity toEntity(RideEstimateCache cache);

    @Mapping(target = "price", source = "finalAmount")
    @Mapping(target = "statusDescription", source = "status.description")
    RideCreateResponseDto toRideCreateResponseDto(RideEntity rideEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "seats", ignore = true)
    void updateRideFromDto(DriverAssignedEvent dto, @MappingTarget RideEntity entity);

    @Mapping(target = "cancelAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "cancelReasonComment", source = "comment")
    void cancelRideFromDto(RideCancelRequestDto dto, @MappingTarget RideEntity entity);

    @Mapping(target = "durationMinutes", source = "rideEntity")
    RideEndResponseDto toRideEndResponseDto(RideEntity rideEntity);

    @Mapping(target = "comment", constant = "available drivers not found")
    RideCancelRequestDto toRideCancelRequestDto(NoDriversEvent noDriversEvent);

    default Long mapDuration(RideEntity entity) {
        return (entity.getStartAt() == null || entity.getEndAt() == null) ?
                null :
                Duration.between(entity.getStartAt(), entity.getEndAt()).toMinutes();
    }

    @Mapping(target = "driver", source = "driverResponseDto")
    @Mapping(target = "reason", source = "rideEntity.cancelReasonComment")
    RideFullResponseDto toRideFullResponseDto(RideEntity rideEntity,
                                              DriverResponseDto driverResponseDto);
}