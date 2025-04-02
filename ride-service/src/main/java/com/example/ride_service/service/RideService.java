package com.example.ride_service.service;

import com.example.ride_service.dto.RatingIdEvent;
import com.example.ride_service.dto.RideCreatedEvent;
import com.example.ride_service.dto.RideDto;
import com.example.ride_service.dto.RideRequestDto;
import com.example.ride_service.entity.RideEntity;
import com.example.ride_service.enums.RideStatus;
import com.example.ride_service.enums.SenderType;
import com.example.ride_service.exception.InvalidStatusException;
import com.example.ride_service.exception.NotFoundException;
import com.example.ride_service.mapper.RideMapper;
import com.example.ride_service.repo.RideRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public String payRide(String rideId, RideStatus newStatus, BigDecimal amount) {
        RideEntity ride = findRideOrThrow(rideId);
        ride.setStatus(newStatus);
        ride.setUpdatedAt(LocalDateTime.now());
        ride.setAmount(amount);
        rideRepo.save(ride);
        return "поездка оплачена!";
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
        ride.setStatus(newStatus);
        ride.setUpdatedAt(LocalDateTime.now());
        if (newStatus == RideStatus.COMPLETED) ride.setCompletedIn(LocalDateTime.now());
        rideRepo.save(ride);
        return "СТАТУС БЫЛ ИЗМЕНЕН УСПЕШНО";
    }

    @KafkaListener(topics = "drivers-not-found",
            groupId = "ride-service-group")
    public void handleCancelledRide(String message) {
        System.out.println("получено сообщение!!! " + message);
        rideRepo.findById(message).ifPresentOrElse(ride -> {
            ride.setStatus(RideStatus.CANCELLED);
            rideRepo.save(ride);
            System.out.println("Поездка " + message + " отменена");
        }, () -> System.out.println("Поездка с ID " + message + " не найдена"));
    }


    public RideDto findRideById(String id) {
        RideEntity ride = rideRepo.findById(id).orElseThrow(()
                -> new NotFoundException("поездка с таким айди: " + id + " не найдено"));
        return mapper.toDto(ride);
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

    @KafkaListener(
            topics = "rating-id-event",
            groupId = "ride-rating-group",
            containerFactory = "ratingIdListenerContainerFactory"
    )
    public void addRatingInRide(RatingIdEvent event) {
        RideEntity ride = rideRepo.findById(event.rideId()).orElseThrow(() -> new NotFoundException("такой поездки не существует"));
        if (event.type() == SenderType.DRIVER) {

            ride.setPassengerRatingId(event.recipientRatingId());
        } else {
            ride.setDriverRatingId(event.recipientRatingId());
        }
        ride.setUpdatedAt(LocalDateTime.now());
        rideRepo.save(ride);
        System.out.println("в поездке " + ride.getId() + " был выставлен рейтинг ");
    }
}
