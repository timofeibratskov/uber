package com.example.ride_service.mapper;

import com.example.ride_service.model.entity.RideEntity;
import com.example.ride_service.model.event.RideCanceledEvent;
import com.example.ride_service.model.event.RideCompletedEvent;
import com.example.ride_service.model.event.RideCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(target = "rideId", source = "id")
    RideCreatedEvent toCreatedEvent(RideEntity rideEntity);

    @Mapping(target = "rideId", source = "rideEntity.id")
    RideCanceledEvent toCancelledEvent(RideEntity rideEntity);

    @Mapping(target = "amount", source = "finalAmount")
    RideCompletedEvent toCompletedEvent(RideEntity rideEntity);
}
