package com.example.passenger_service.service;

import com.example.passenger_service.client.RatingServiceClient;
import com.example.passenger_service.exception.AlreadyExistsException;
import com.example.passenger_service.exception.PassengerNotFoundException;
import com.example.passenger_service.mapper.PassengerMapper;
import com.example.passenger_service.model.dto.CompleteProfileRequestDto;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.UpdatePassengerDto;
import com.example.passenger_service.model.events.UserRegisteredEvent;
import com.example.passenger_service.repo.PassengerRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class PassengerService {
    private final PassengerRepo passengerRepo;
    private final PassengerMapper passengerMapper;
    private final RatingServiceClient ratingServiceClient;

    @Transactional
    public String completeProfile(
            UUID id,
            CompleteProfileRequestDto request) {

        var passenger = passengerRepo.findById(id)
                .orElseThrow(() ->
                        new PassengerNotFoundException("passenger with id=" + id + "not found!"));

        if (passengerRepo.existsByPhoneNumber(request.phoneNumber())) {
            throw new AlreadyExistsException("Phone number already exists!");
        }

        passengerMapper.updateEntity(passenger, request);

        passengerRepo.save(passenger);
        log.info("Passenger profile successfully completed. email: {}", passenger.getEmail());

        return "Hi," + passenger.getName() + ", your profile successfully saved!";
    }

    @Transactional
    public void save(UserRegisteredEvent event) {
        passengerRepo.findByEmail(event.email())
                .ifPresentOrElse(existingPassenger -> {
                            if (existingPassenger.getId().equals(event.userId())) {
                                log.info("этот пассажир уже существует");
                            } else {
                                log.info("почта: {} уже занята", event.email());
                            }
                        },
                        () -> {
                            var passenger = passengerMapper.toEntity(event);
                            passenger.setNew(true);
                            passengerRepo.save(passenger);
                        }
                );
    }

    @Transactional(readOnly = true)
    public PassengerResponseDto findPassengerById(UUID id) {
        var passenger = passengerRepo.findById(id)
                .orElseThrow(() -> {
                    log.info("Incorrect id: {}", id);
                    return new PassengerNotFoundException("Passenger not found!");
                });
        try {
            var ratingResponse = ratingServiceClient.getUserRating(id).getBody();
            var rating = (ratingResponse != null) ? ratingResponse.rating() : BigDecimal.ZERO;
            return passengerMapper.toResponseDto(passenger, rating);
        } catch (Exception e) {
            log.error("Error fetching rating for passenger {}: {}", id, e.getMessage());
            return passengerMapper.toResponseDto(passenger, BigDecimal.ZERO);
        }
    }

    @Transactional
    public void updatePassenger(UUID id, UpdatePassengerDto updatePassenger) {
        var passenger = passengerRepo.findById(id)
                .orElseThrow(() -> {
                            log.info("Update failed: Passenger with id {} not found", id);
                            return new PassengerNotFoundException("Passenger not found!");
                        }
                );
        if (updatePassenger.phoneNumber() != null &&
                !updatePassenger.phoneNumber().equals(passenger.getPhoneNumber())) {
            if (passengerRepo.existsByPhoneNumber(updatePassenger.phoneNumber())) {
                throw new AlreadyExistsException("Phone number " + updatePassenger.phoneNumber() + " already exists!");
            }
            passenger.setPhoneNumber(updatePassenger.phoneNumber());
        }
        if (updatePassenger.name() != null) {
            passenger.setName(updatePassenger.name());
        }
        if (updatePassenger.gender() != null) {
            passenger.setGender(updatePassenger.gender());
        }
        log.info("Updating passenger with id {}", id);
    }
}
