package com.example.ride_service.mapper;

import com.example.ride_service.dto.RideCreatedEvent;
import com.example.ride_service.dto.RideDto;
import com.example.ride_service.dto.RideRequestDto;
import com.example.ride_service.entity.RideEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RideMapper {
    public RideEntity toEntity(RideRequestDto request) {
        return RideEntity.builder()
                .pointA(request.getPointA())
                .pointB(request.getPointB())
                .creatorId(request.getCreatorId())
                .seats(request.getSeats())
                .build();
    }

    public RideDto toDto(RideEntity entity) {
        return RideDto.builder()
                .id(entity.getId())
                .pointA(entity.getPointA())
                .pointB(entity.getPointB())
                .creatorId(entity.getCreatorId())
                .seats(entity.getSeats())
                .driverId(entity.getDriverId())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .completedIn(entity.getCompletedIn())
                .passengerRatingId(entity.getPassengerRatingId())
                .driverRatingId(entity.getDriverRatingId())
                .build();
    }
    public RideCreatedEvent requestToEvent(RideRequestDto request){
        return RideCreatedEvent.builder()
                .pointA(request.getPointA())
                .pointB(request.getPointB())
                .creatorId(request.getCreatorId())
                .seats(request.getSeats())
                .time(LocalDateTime.now())
                .build();
    }
}
