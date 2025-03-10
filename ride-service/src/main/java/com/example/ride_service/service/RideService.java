package com.example.ride_service.service;

import com.example.ride_service.dto.RideCreatedEvent;
import com.example.ride_service.dto.RideDto;
import com.example.ride_service.dto.RideRequestDto;
import com.example.ride_service.entity.RideEntity;
import com.example.ride_service.entity.RideStatus;
import com.example.ride_service.exception.InvalidStatusException;
import com.example.ride_service.exception.NotFoundException;
import com.example.ride_service.mapper.RideMapper;
import com.example.ride_service.repo.RideRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RideService {
    private final RideRepo rideRepo;
    private final RideMapper mapper;
    private final KafkaTemplate<String, RideCreatedEvent> kafkaTemplate;

    public RideStatus createRide(RideRequestDto request) {
        RideEntity ride = mapper.toEntity(request);
        String id = rideRepo.save(ride).getId();
        ride.setStatus(RideStatus.CREATED);
        ride.setCreatedAt(LocalDateTime.now());
        ride.setUpdatedAt(LocalDateTime.now());
        RideCreatedEvent event = mapper.requestToEvent(request);
        event.setId(id);
        rideRepo.save(ride);
        kafkaTemplate.send("ride-created", event);
        System.out.println(ride.toString());
        return ride.getStatus();
    }

    public void assignDriver(String rideId, Long driverId) {
        RideEntity ride = findRideOrThrow(rideId);
        if (ride.getStatus() != RideStatus.CREATED) {
            throw new InvalidStatusException("водителя можно назначить только на CREATED поездку");
        }
        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.DRIVER_FOUND);
        ride.setUpdatedAt(LocalDateTime.now());
         rideRepo.save(ride);
        System.out.println(ride.toString());
    }

    public String changeStatus(String rideId, RideStatus newStatus) {
        RideEntity ride = findRideOrThrow(rideId);
        validateStatusTransition(ride.getStatus(), newStatus);
        ride.setStatus(newStatus);
        ride.setUpdatedAt(LocalDateTime.now());
        rideRepo.save(ride);
        if (newStatus == RideStatus.COMPLETED) ride.setCompletedIn(LocalDateTime.now());
        return "СТАТУС БЫЛ ИЗМЕНЕН УСПЕШНО";
    }

    @KafkaListener(topics = "drivers-not-found", groupId = "ride-service-group")
    public void handleCancelledRide(String message) {
        System.out.println("получено сообщение!!! " + message);
        rideRepo.findById(message).ifPresentOrElse(ride -> {
            ride.setStatus(RideStatus.CANCELLED);
            rideRepo.save(ride);
            System.out.println("Поездка " + message + " отменена");
        }, () -> System.out.println("Поездка с ID " + message + " не найдена"));
    }

    private void validateStatusTransition(RideStatus current, RideStatus newStatus) {
        if (newStatus == RideStatus.PAID && current != RideStatus.COMPLETED) {
            throw new InvalidStatusException("Оплата должна быть после COMPLETED");
        }

//        if (current == RideStatus.CANCELLED) {
//            throw new InvalidStatusException("Поездка отменена. Создайте новую поездку");
//        }
//        if (current == RideStatus.CREATED && newStatus != RideStatus.DRIVER_FOUND) {
//            throw new InvalidStatusException("статус должен поменяться только на DRIVER_FOUND");
//        }
//        if (current == RideStatus.DRIVER_FOUND && newStatus != RideStatus.IN_PROGRESS) {
//            throw new InvalidStatusException("статус должен поменяться только на IN_PROGRESS");
//        }
//        if (current == RideStatus.COMPLETED && newStatus != RideStatus.PAID) {
//            throw new InvalidStatusException("статус должен поменяться только на PAID");
//        }
    }

    public List<RideDto> getRidesByPassengerId(Long id) {
        List<RideDto> rides = rideRepo.findAllByCreatorId(id)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        if (rides.isEmpty()) throw new NotFoundException("Поезки для пассажира с таким id не существует");
        return rides;
    }

    public List<RideDto> getRidesByDriverId(Long id) {
        List<RideDto> rides = rideRepo.findAllByDriverId(id).stream().map(mapper::toDto).collect(Collectors.toList());
        if (rides.isEmpty()) throw new NotFoundException("Поезки для водителя с таким id не существует");
        return rides;
    }

    public List<RideDto> getRidesByStatus(RideStatus status) {
        List<RideDto> rides = rideRepo.findByStatus(status).stream().map(mapper::toDto).collect(Collectors.toList());
        if (rides.isEmpty()) throw new NotFoundException("Поезки с таким статусом пока что не существует");
        return rides;
    }

    private RideEntity findRideOrThrow(String rideId) {
        return rideRepo.findById(rideId)
                .orElseThrow(() -> new NotFoundException("Поездка не найдена"));
    }
}
