package com.example.passenger_service.service;

import com.example.passenger_service.dto.DeletePassengerDto;
import com.example.passenger_service.dto.PassengerDto;
import com.example.passenger_service.dto.RegisterPassengerDto;
import com.example.passenger_service.dto.LoginPassengerDto;
import com.example.passenger_service.entity.PassengerEntity;
import com.example.passenger_service.exception.InvalidCredentialsException;
import com.example.passenger_service.exception.PassengerAlreadyExistsException;
import com.example.passenger_service.exception.PassengerNotFoundException;
import com.example.passenger_service.repo.PassengerRepo;
import com.example.passenger_service.mapper.PassengerMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PassengerService {

    private final PassengerRepo passengerRepo;
    private final PassengerMapper passengerMapper;

    public PassengerService(PassengerRepo passengerRepo, PassengerMapper passengerMapper) {
        this.passengerRepo = passengerRepo;
        this.passengerMapper = passengerMapper;
    }

    public List<PassengerDto> findAllPassengers() {
        List<PassengerEntity> passengers = passengerRepo.findAll();
        System.out.println(passengers);
        return passengers.stream()
                .map(passengerMapper::toPassengerDto)
                .collect(Collectors.toList());
    }

    public RegisterPassengerDto registerPassenger(RegisterPassengerDto registerPassengerDto) {
        PassengerEntity passenger = passengerMapper.toPassenger(registerPassengerDto);

        passengerRepo.findPassengerByGmail(passenger.getGmail())
                .ifPresent(existingPassenger -> {
                    throw new PassengerAlreadyExistsException(String.format("Пассажир с email %s уже зарегистрирован.", passenger.getGmail()));
                });

        passengerRepo.findPassengerByName(passenger.getName())
                .ifPresent(existingPassenger -> {
                    throw new PassengerAlreadyExistsException(String.format("Пассажир с именем %s уже зарегистрирован.", passenger.getName()));
                });

        PassengerEntity savedPassenger = passengerRepo.save(passenger);
        return passengerMapper.toRegisterPassengerDto(savedPassenger);
    }

    public PassengerDto loginPassenger(LoginPassengerDto loginPassengerDto) {
        return passengerMapper.toPassengerDto(passengerRepo.findPassengerByGmail(loginPassengerDto.getGmail())
                .filter(passenger -> passenger.getPassword().equals(loginPassengerDto.getPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("Неверное имя пользователя или пароль."))
        );
    }

    public RegisterPassengerDto findPassenger(Long id) {
        PassengerEntity passenger = passengerRepo.findPassengerById(id)
                .orElseThrow(() -> new PassengerNotFoundException(String.format("Пассажир с ID: %d не существует", id)));
        return passengerMapper.toRegisterPassengerDto(passenger);
    }


    public void deletePassenger(DeletePassengerDto deletePassengerDto) {
        PassengerEntity passenger = passengerRepo.findPassengerByGmail(deletePassengerDto.getGmail())
                .orElseThrow(() -> new InvalidCredentialsException("Пользователь не найден."));

        if (!passenger.getPassword().equals(deletePassengerDto.getPassword())) {
            throw new InvalidCredentialsException("Неверное имя пользователя или пароль.");
        }

        passengerRepo.delete(passenger);
    }
}