package com.example.ride_service.service;

import com.example.ride_service.client.DriverServiceClient;
import com.example.ride_service.exception.EstimateExpiredException;
import com.example.ride_service.exception.InvalidStatusTransitionException;
import com.example.ride_service.exception.RideNotFoundException;
import com.example.ride_service.mapper.EventMapper;
import com.example.ride_service.mapper.RideMapper;
import com.example.ride_service.model.dto.RideCancelRequestDto;
import com.example.ride_service.model.dto.RideCreateRequestDto;
import com.example.ride_service.model.dto.RideCreateResponseDto;
import com.example.ride_service.model.dto.RideEndResponseDto;
import com.example.ride_service.model.dto.RideFullResponseDto;
import com.example.ride_service.model.enums.EventType;
import com.example.ride_service.model.enums.PaymentStatus;
import com.example.ride_service.model.enums.RideStatus;
import com.example.ride_service.model.enums.TopicType;
import com.example.ride_service.model.event.DriverAssignedEvent;
import com.example.ride_service.repo.db.RideRepo;
import com.example.ride_service.repo.redis.RideEstimateCacheRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {
    private final RideRepo rideRepo;
    private final RideEstimateCacheRepo rideEstimateCacheRepo;
    private final RideMapper rideMapper;
    private final OutboxService outboxService;
    private final RideStateMachine rideStateMachine;
    private final EventMapper eventMapper;
    private final DriverServiceClient driverServiceClient;

    @Transactional
    public RideCreateResponseDto create(RideCreateRequestDto request) {
        var cache = rideEstimateCacheRepo.findById(request.passengerId())
                .orElseThrow(() -> new EstimateExpiredException("The preliminary estimate has expired. Please recalculate your ride"));
        log.info("ride with id {} found in cache", cache.getPassengerId());

        var ride = rideMapper.toEntity(cache);
        ride.setPaymentStatus(PaymentStatus.NOT_PAID);
        ride.setSeats(request.seats());
        rideStateMachine.changeRideStatus(ride, RideStatus.CREATED);

        var savedRide = rideRepo.save(ride);
        rideEstimateCacheRepo.delete(cache);
        log.info("ride with id {} saved successfully", savedRide.getId());

        var event = eventMapper.toCreatedEvent(savedRide);
        outboxService.saveEvent(event, EventType.RIDE_CREATED, TopicType.RIDE_LIFECYCLE);

        return rideMapper.toRideCreateResponseDto(savedRide);
    }

    @Transactional
    public void accept(DriverAssignedEvent request) {
        var ride = rideRepo.findById(request.rideId())
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        rideStateMachine.changeRideStatus(ride, RideStatus.ACCEPTED);
        rideMapper.updateRideFromDto(request, ride);

        log.info("ride with id {} accepted successfully", ride.getId());
    }

    @Transactional
    public void cancel(UUID rideId,
                       RideCancelRequestDto request) {
        var ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        rideMapper.cancelRideFromDto(request, ride);

        rideStateMachine.changeRideStatus(ride, RideStatus.CANCELLED);

        if (ride.getDriverId() != null) {
            var event = eventMapper.toCancelledEvent(ride);
            outboxService.saveEvent(event, EventType.RIDE_CANCELLED, TopicType.RIDE_LIFECYCLE);
        }

        log.info("ride with id {} cancelled successfully", ride.getId());
    }

    @Transactional
    public void start(UUID rideId) {
        var ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        rideStateMachine.changeRideStatus(ride, RideStatus.STARTED);

        ride.setStartAt(LocalDateTime.now());
        log.info("ride with id {} started successfully", ride.getId());
    }

    @Transactional
    public RideEndResponseDto complete(UUID rideId) {
        var ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        rideStateMachine.changeRideStatus(ride, RideStatus.COMPLETED);

        var event = eventMapper.toCompletedEvent(ride);


        outboxService.saveEvent(event, EventType.RIDE_COMPLETED, TopicType.RIDE_LIFECYCLE);

        ride.setPaymentStatus(PaymentStatus.PROCESSING);

        ride.setEndAt(LocalDateTime.now());
        log.info("ride with id {} completed successfully", ride.getId());

        return rideMapper.toRideEndResponseDto(ride);
    }

    @Transactional(readOnly = true)
    public RideFullResponseDto findById(UUID rideId) {
        var ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        if (ride.getDriverId() != null) {
            var driverInfo = driverServiceClient.getDriverById(ride.getDriverId());
            return rideMapper.toRideFullResponseDto(ride, driverInfo);
        }

        return rideMapper.toRideFullResponseDto(ride, null);
    }
}