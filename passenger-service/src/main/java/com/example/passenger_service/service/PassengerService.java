package com.example.passenger_service.service;

import com.example.passenger_service.client.RatingServiceClient;
import com.example.passenger_service.exception.AlreadyExistsException;
import com.example.passenger_service.exception.InvalidCredentialsException;
import com.example.passenger_service.exception.PassengerNotFoundException;
import com.example.passenger_service.mapper.PassengerMapper;
import com.example.passenger_service.model.dto.LoginPassengerDto;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.RegisterPassengerDto;
import com.example.passenger_service.model.dto.UpdatePassengerDto;
import com.example.passenger_service.repo.PassengerRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final RatingServiceClient ratingServiceClient;

    @Transactional
    public String registerPassenger(RegisterPassengerDto request) {
        if (passengerRepo.existsByEmail(request.email())) {
            throw new AlreadyExistsException("Email already exists!");
        }
        if (passengerRepo.existsByPhoneNumber(request.phoneNumber())) {
            throw new AlreadyExistsException("Phone number already exists!");
        }
        var passenger = passengerMapper.toEntity(request);
        passenger.setPassword(passwordEncoder.encode(request.password()));

        var savedPassenger = passengerRepo.save(passenger);
        log.info("Passenger registered successfully with email: {}", request.email());

        return "Hi," + savedPassenger.getName() + ", you are registered successfully!";
    }

    @Transactional(readOnly = true)
    public String loginPassenger(LoginPassengerDto request) {
        var passenger = passengerRepo.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.info("Incorrect email: {}", request.email());
                    return new InvalidCredentialsException("Incorrect email or password!");
                });
        if (passwordEncoder.matches(request.password(), passenger.getPassword())) {
            return "Hi," + passenger.getName() + ", you are with us again!";
        } else {
            log.info("Incorrect password, email: {}", request.email());
            throw new InvalidCredentialsException("Incorrect email or password!");
        }
    }

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
