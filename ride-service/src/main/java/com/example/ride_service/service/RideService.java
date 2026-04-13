package com.example.ride_service.service;

import com.example.ride_service.client.OpenRouteServiceClient;
import com.example.ride_service.exception.EstimateExpiredException;
import com.example.ride_service.exception.InvalidStatusTransitionException;
import com.example.ride_service.exception.RideNotFoundException;
import com.example.ride_service.mapper.RideMapper;
import com.example.ride_service.model.dto.RideAcceptedRequestDto;
import com.example.ride_service.model.dto.RideAcceptedResponseDto;
import com.example.ride_service.model.dto.RideCancelRequestDto;
import com.example.ride_service.model.dto.RideCreateRequestDto;
import com.example.ride_service.model.dto.RideCreateResponseDto;
import com.example.ride_service.model.dto.RideEstimateRequestDto;
import com.example.ride_service.model.dto.RideEstimateResponseDto;
import com.example.ride_service.model.enums.RideStatus;
import com.example.ride_service.repo.db.RideRepo;
import com.example.ride_service.repo.redis.RideEstimateCacheRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {
    private final RideRepo rideRepo;
    private final RideEstimateCacheRepo rideEstimateCacheRepo;
    private final RideMapper mapper;
    private final OpenRouteServiceClient openRouteServiceClient;

    public RideEstimateResponseDto calculateRide(RideEstimateRequestDto request) {
        var jsonResponse = openRouteServiceClient.fetchRoute(request.startPoint(), request.stopPoint());
        log.info("Получен ответ от ORS для маршрута");

        double distanceMeters = jsonResponse.at("/routes/0/summary/distance").asDouble();
        double durationSeconds = jsonResponse.at("/routes/0/summary/duration").asDouble();
        String geometry = jsonResponse.at("/routes/0/geometry").asText();
        double distanceKm = distanceMeters / 1000.0;
        long durationMin = Math.round(durationSeconds / 60.0);

        BigDecimal price = BigDecimal.valueOf(3.0)
                .add(BigDecimal.valueOf(distanceKm)
                        .multiply(BigDecimal.valueOf(0.8)))
                .setScale(2, RoundingMode.HALF_UP);

        var estimateDto = RideEstimateResponseDto.builder()
                .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                .durationMin(durationMin)
                .price(price)
                .polyline(geometry)
                .build();

        var estimateCache = mapper.toCache(estimateDto, request.passengerId());
        rideEstimateCacheRepo.save(estimateCache);
        log.info("passenger with id {} saved cache successfully", request.passengerId());
        return estimateDto;
    }

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
        return mapper.toRideCreateResponseDto(savedEntity);
    }

    @Transactional
    public RideAcceptedResponseDto acceptRide(RideAcceptedRequestDto request) {
        var ride = rideRepo.findById(request.rideId())
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        if (ride.getStatus() != RideStatus.CREATED)
            throw new InvalidStatusTransitionException("invalid ride status");

        mapper.updateRideFromDto(request, ride);
        ride.setStatus(RideStatus.ACCEPTED);
        log.info("ride with id {} accepted successfully", ride.getId());

        return mapper.toRideAcceptedResponseDto(ride);
    }

    @Transactional
    public void cancelRide(UUID rideId,
                           RideCancelRequestDto request) {
        var ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("ride not found"));

        if (ride.getStatus() == RideStatus.ACCEPTED || ride.getStatus() == RideStatus.CREATED) {
            mapper.cancelRideFromDto(request, ride);
            ride.setStatus(RideStatus.CANCELLED);
        } else throw new InvalidStatusTransitionException("invalid ride status");
    }
}