package com.example.passenger_service.service;

import com.example.passenger_service.dto.LoginPassengerRequest;
import com.example.passenger_service.dto.PassengerDto;
import com.example.passenger_service.dto.PassengerRatingEvent;
import com.example.passenger_service.dto.PassengerRequest;
import com.example.passenger_service.entity.PassengerEntity;
import com.example.passenger_service.exception.InvalidCredentialsException;
import com.example.passenger_service.exception.ResourceAlreadyExistsException;
import com.example.passenger_service.repo.PassengerRepo;
import com.example.passenger_service.mapper.PassengerMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PassengerService {
    private final PassengerRepo passengerRepo;

    private final PassengerMapper passengerMapper;


    public PassengerDto findPassenger(Long id) {
        PassengerEntity passenger = passengerRepo.findPassengerById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Пассажир с ID: %d не существует", id)));
        return passengerMapper.toDto(passenger);
    }

    public List<PassengerDto> findAllPassengers() {
        List<PassengerEntity> passengers = passengerRepo.findAll();
        return passengers.stream()
                .map(passengerMapper::toDto)
                .collect(Collectors.toList());
    }

    public String registerPassenger(PassengerRequest request) {
        PassengerEntity passenger = passengerMapper.toEntity(request);
        System.out.println(request.gmail() + " " + request.name());
        passengerRepo.findPassengerByGmail(request.gmail())
                .ifPresent(existingPassenger -> {
                    throw new ResourceAlreadyExistsException(String.format("Пассажир с email %s уже зарегистрирован.", request.gmail()));
                });

        passengerRepo.findPassengerByName(request.name())
                .ifPresent(existingPassenger -> {
                    throw new ResourceAlreadyExistsException(String.format("Пассажир с именем %s уже зарегистрирован.", request.name()));
                });
        passengerRepo.findPassengerByPhoneNumber(request.phoneNumber())
                .ifPresent(existingPassenger -> {
                    throw new ResourceAlreadyExistsException(String.format("Пассажир с телефоном %s уже зарегистрирован.", request.phoneNumber()));
                });
        passengerRepo.save(passenger);
        return "АККАУНТ УСПЕШНО СОЗДАН!";
    }


    public PassengerDto loginPassenger(LoginPassengerRequest request) {
        return passengerMapper.toDto(passengerRepo.findPassengerByGmail(request.gmail())
                .filter(passenger -> passenger.getPassword().equals(request.password()))
                .orElseThrow(() -> new InvalidCredentialsException("Неверное имя пользователя или пароль."))

        );
    }

    public String updatePassenger(Long id, PassengerRequest request) {
        PassengerEntity existingPassenger = passengerRepo.findPassengerById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пассажир с ID " + id + " не найден."));
        if (request.gmail() != null && !request.gmail().equals(existingPassenger.getGmail())) {
            passengerRepo.findPassengerByGmail(request.gmail())
                    .ifPresent(p -> {
                        if (!p.getId().equals(id)) {
                            throw new ResourceAlreadyExistsException("Пассажир с таким email уже существует.");
                        }
                    });
            existingPassenger.setGmail(request.gmail());
        }
        if (request.name() != null && !request.name().equals(existingPassenger.getName())) {
            passengerRepo.findPassengerByName(request.name())
                    .ifPresent(p -> {
                        if (!p.getId().equals(id)) {
                            throw new ResourceAlreadyExistsException("Пассажир с таким именем уже существует.");
                        }
                    });
            existingPassenger.setName(request.name());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().equals(existingPassenger.getPhoneNumber())) {
            passengerRepo.findPassengerByPhoneNumber(request.phoneNumber())
                    .ifPresent(p -> {
                        if (!p.getId().equals(id)) {
                            throw new ResourceAlreadyExistsException("Пассажир с таким номером телефона уже существует.");
                        }
                    });
            existingPassenger.setPhoneNumber(request.phoneNumber());
        }
        if (request.password() != null) {
            existingPassenger.setPassword(request.password());
        }
        passengerRepo.save(existingPassenger);
        return "ОБНОВЛЕНО УСПЕШНО";
    }


    public void deletePassenger(Long id) {
        PassengerEntity passenger = passengerRepo.findPassengerById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден."));
        passengerRepo.delete(passenger);
    }

    @KafkaListener(
            topics = "PASSENGER-rating-event",
            groupId = "passenger-rating-group",
            containerFactory = "passengerRatingListenerContainerFactory"
    )
    public void updatePassengerRating(PassengerRatingEvent event) {
        PassengerEntity passenger = passengerRepo.findPassengerById(event.recipientId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        if (event.rating()==null){
            throw new IllegalArgumentException("Rating cannot be null");
        }
        passenger.setRating(event.rating());
        passengerRepo.save(passenger);

        System.out.printf("Пассажир с id %d обновлен! Новый рейтинг: %.2f%n",
                passenger.getId(), passenger.getRating());
    }
}
