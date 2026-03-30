package com.example.passenger_service.service;

import com.example.passenger_service.exception.AlreadyExistsException;
import com.example.passenger_service.mapper.PassengerMapper;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.RegisterPassengerDto;
import com.example.passenger_service.repo.PassengerRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
@Slf4j
public class PassengerService {
    private final PassengerRepo passengerRepo;
    private final PassengerMapper passengerMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PassengerResponseDto registerPassenger(RegisterPassengerDto request) {
        if (passengerRepo.existsByEmail(request.email())) {
            throw new AlreadyExistsException("Email already exists!");
        }
        if (passengerRepo.existsByPhoneNumber(request.phoneNumber())) {
            throw new AlreadyExistsException("Phone number already exists!");
        }
        var passenger = passengerMapper.toEntity(request);
        passenger.setPassword(passwordEncoder.encode(request.password()));
        passenger.setRating(BigDecimal.ZERO);

        var savedPassenger = passengerRepo.save(passenger);
        log.info("Passenger registered successfully with email: {}", request.email());
        return passengerMapper.toResponseDto(savedPassenger);
    }
}
