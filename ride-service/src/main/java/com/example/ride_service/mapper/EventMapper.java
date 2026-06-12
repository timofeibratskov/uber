package com.example.ride_service.mapper;

import com.example.ride_service.model.entity.RideEntity;
import com.example.ride_service.model.event.RideCancelledEvent;
import com.example.ride_service.model.event.RideCreateEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(target = "rideId", source = "id")
    RideCreateEvent toCreatedEvent(RideEntity rideEntity);

    @Mapping(target = "rideId", source = "rideEntity.id")
    @Mapping(target = "driverId", source = "rideEntity.driverId")
    @Mapping(target = "cancelAt", source = "rideEntity.cancelAt")
    RideCancelledEvent toCancelledEvent(RideEntity rideEntity);
}
