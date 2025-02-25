package com.example.ride_service.service;

import com.example.ride_service.dto.RideRequestDto;
import com.example.ride_service.entity.RideEntity;
import com.example.ride_service.entity.RideStatus;
import com.example.ride_service.exception.InvalidStatusException;
import com.example.ride_service.exception.NotFoundException;
import com.example.ride_service.repo.RideRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RideService {
    private final RideRepo rideRepo;

    public RideEntity createRide(RideRequestDto request) {
        RideEntity ride = RideEntity.builder()
                .pointA(request.getPointA())
                .pointB(request.getPointB())
                .creatorId(request.getCreatorId())
                .seats(request.getSeats())
                .status(RideStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();
        return rideRepo.save(ride);
    }

    public RideEntity assignDriver(String rideId, Long driverId) {
        RideEntity ride = findRideOrThrow(rideId);
        if (ride.getStatus() != RideStatus.CREATED) {
            throw new InvalidStatusException("водителя можно назначить только на CREATED поездку");
        }
        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.DRIVER_FOUND);
        ride.setUpdatedAt(LocalDateTime.now());
        return rideRepo.save(ride);
    }

    public RideEntity changeStatus(String rideId, RideStatus newStatus) {
        RideEntity ride = findRideOrThrow(rideId);
        validateStatusTransition(ride.getStatus(), newStatus);
        ride.setStatus(newStatus);
        ride.setUpdatedAt(LocalDateTime.now());
        if (newStatus == RideStatus.COMPLETED) ride.setCompletedIn(LocalDateTime.now());
        return rideRepo.save(ride);
    }

    private void validateStatusTransition(RideStatus current, RideStatus newStatus) {
        if (newStatus == RideStatus.PAID && current != RideStatus.COMPLETED) {
            throw new InvalidStatusException("Оплата должна быть после COMPLETED");
        }

        if (current == RideStatus.CANCELLED) {
            throw new InvalidStatusException("Поездка отменена. Создайте новую поездку");
        }
        if (current == RideStatus.CREATED && newStatus != RideStatus.DRIVER_FOUND) {
            throw new InvalidStatusException("статус должен поменяться только на DRIVER_FOUND");
        }
        if (current == RideStatus.DRIVER_FOUND && newStatus != RideStatus.IN_PROGRESS) {
            throw new InvalidStatusException("статус должен поменяться только на IN_PROGRESS");
        }
        if (current == RideStatus.COMPLETED && newStatus != RideStatus.PAID) {
            throw new InvalidStatusException("статус должен поменяться только на PAID");
        }
    }

    public List<RideEntity> getRidesByPassengerId(Long id) {
        List<RideEntity> rides = rideRepo.findAllByCreatorId(id);
        if (rides.isEmpty())throw new NotFoundException("Поезки для пассажира с таким id не существует");
        return rides;
    }

    public List<RideEntity> getRidesByDriverId(Long id) {
        List<RideEntity> rides = rideRepo.findAllByDriverId(id);
        if (rides.isEmpty())throw new NotFoundException("Поезки для водителя с таким id не существует");
        return rides; }

    public List<RideEntity> getRidesByStatus(RideStatus status) {
        List<RideEntity> rides = rideRepo.findByStatus(status);
        if (rides.isEmpty())throw new NotFoundException("Поезки с таким статусом пока что не существует");
        return rides; }

    private RideEntity findRideOrThrow(String rideId) {
        return rideRepo.findById(rideId)
                .orElseThrow(() -> new NotFoundException("Поездка не найдена"));
    }
}
