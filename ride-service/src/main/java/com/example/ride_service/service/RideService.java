package com.example.ride_service.service;

import com.example.ride_service.client.OpenRouteServiceClient;
import com.example.ride_service.exception.EstimateExpiredException;
import com.example.ride_service.exception.InvalidStatusTransitionException;
import com.example.ride_service.exception.RideNotFoundException;
import com.example.ride_service.mapper.RideMapper;
import com.example.ride_service.model.dto.RideCancelRequestDto;
import com.example.ride_service.model.dto.RideCreateRequestDto;
import com.example.ride_service.model.dto.RideCreateResponseDto;
import com.example.ride_service.model.dto.RideEndResponseDto;
import com.example.ride_service.model.dto.RideEstimateRequestDto;
import com.example.ride_service.model.dto.RideEstimateResponseDto;
import com.example.ride_service.model.enums.EventType;
import com.example.ride_service.model.enums.RideStatus;
import com.example.ride_service.model.enums.TopicType;
import com.example.ride_service.model.event.DriverAssignedEvent;
import com.example.ride_service.model.event.RideCreateEvent;
import com.example.ride_service.repo.db.RideRepo;
import com.example.ride_service.repo.redis.RideEstimateCacheRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {
    private final RideRepo rideRepo;
    private final RideEstimateCacheRepo rideEstimateCacheRepo;
    private final RideMapper mapper;
    private final OutboxService outboxService;

    @Transactional
    public RideCreateResponseDto createRide(RideCreateRequestDto request) {
        var cache = rideEstimateCacheRepo.findById(request.passengerId())
                .orElseThrow(() -> new EstimateExpiredException("The preliminary estimate has expired. Please recalculate your ride"));
        log.info("ride with id {} found in cache", cache.getPassengerId());

        var entity = mapper.toEntity(cache);
        entity.setSeats(request.seats());

        var savedEntity = rideRepo.save(entity);
        rideEstimateCacheRepo.delete(cache);
        log.info("ride with id {} saved successfully", savedEntity.getId());

        var event = RideCreateEvent.builder()
                .rideId(savedEntity.getId())
                .seats(savedEntity.getSeats())
                .startPoint(savedEntity.getStartPoint())
                .build();

        outboxService.saveEvent(event, EventType.RIDE_CREATED, TopicType.RIDE_LIFECYCLE);

        return mapper.toRideCreateResponseDto(savedEntity);
    }

    @Transactional
    public void acceptRide(DriverAssignedEvent request) {
        var ride = rideRepo.findById(request.rideId())
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        if (ride.getStatus() != RideStatus.CREATED)
            throw new InvalidStatusTransitionException("invalid ride status");

        mapper.updateRideFromDto(request, ride);

        ride.setStatus(RideStatus.ACCEPTED);
        log.info("ride with id {} accepted successfully", ride.getId());
    }

    @Transactional
    public void cancelRide(UUID rideId,
                           RideCancelRequestDto request) {
        var ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        if (ride.getStatus() == RideStatus.ACCEPTED || ride.getStatus() == RideStatus.CREATED) {
            mapper.cancelRideFromDto(request, ride);

            if (ride.getDriverId() != null) {
                var event = mapper.toRideCancelledEvent(ride);
                outboxService.saveEvent(event, EventType.RIDE_CANCELLED, TopicType.RIDE_LIFECYCLE);
                log.info("ride with id {} cancelled successfully by {}", ride.getId(), request.cancelInitiator());
            }
        } else {
            log.error("ride with id: {} has not valid status for canceling: {}", ride.getId(), ride.getStatus());
            throw new InvalidStatusTransitionException("invalid ride status");
        }
    }

    @Transactional
    public void startRide(UUID rideId) {
        var ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            log.error("ride with id: {} has not valid status for starting: {}", ride.getId(), ride.getStatus());
            throw new InvalidStatusTransitionException("invalid ride status");
        }

        ride.setStatus(RideStatus.STARTED);
        ride.setStartAt(LocalDateTime.now());
        log.info("ride with id {} started successfully", ride.getId());
    }

    @Transactional
    public RideEndResponseDto endRide(UUID rideId) {
        var ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        if (ride.getStatus() != RideStatus.STARTED) {
            log.error("ride has not valid status for completing: {}", ride.getStatus());
            throw new InvalidStatusTransitionException("invalid ride status");
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setEndAt(LocalDateTime.now());
        log.info("ride with id {} completed successfully", ride.getId());

        return mapper.toRideEndResponseDto(ride);
    }

    @Transactional(readOnly = true)
    public boolean canPayRide(UUID rideId) {
        var ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        return !ride.isPaid() && ride.getStatus() == RideStatus.COMPLETED;
    }

    @Transactional
    public void payRide(UUID rideId) {
        var ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        ride.setPaid(true);
        log.info("ride with id {} paid successfully", ride.getId());
    }
}