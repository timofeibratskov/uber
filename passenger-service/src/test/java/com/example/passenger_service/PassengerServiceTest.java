package com.example.passenger_service;

import com.example.passenger_service.dto.PassengerRequest;
import com.example.passenger_service.dto.PassengerDto;
import com.example.passenger_service.dto.LoginPassengerRequest;
import com.example.passenger_service.dto.PassengerRatingEvent;
import com.example.passenger_service.entity.PassengerEntity;
import com.example.passenger_service.exception.InvalidCredentialsException;
import com.example.passenger_service.exception.ResourceAlreadyExistsException;
import com.example.passenger_service.repo.PassengerRepo;
import com.example.passenger_service.mapper.PassengerMapper;
import com.example.passenger_service.service.PassengerService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PassengerServiceTest {

    @Mock
    private PassengerRepo passengerRepo;

    @Mock
    private PassengerMapper passengerMapper;

    @InjectMocks
    private PassengerService passengerService;

    private PassengerEntity passenger;
    private PassengerDto passengerDto;

    @BeforeEach
    void setUp() {
        passenger = new PassengerEntity();
        passenger.setId(1L);
        passenger.setName("John Doe");
        passenger.setGmail("john.doe@example.com");
        passenger.setPhoneNumber("1234567890");
        passenger.setPassword("password123");

        passengerDto = new PassengerDto(1L, "John Doe", "john.doe@example.com", "1234567890", "+3211231231", 1L, 5.0F);
    }

    private void mockPassengerExists() {
        when(passengerRepo.findPassengerById(1L)).thenReturn(Optional.of(passenger));
    }

    @Test
    void findPassenger_Success() {
        mockPassengerExists();
        when(passengerMapper.toDto(passenger)).thenReturn(passengerDto);

        PassengerDto result = passengerService.findPassenger(1L);

        assertEquals(passengerDto, result);
        verify(passengerRepo).findPassengerById(1L);
    }

    @Test
    void findPassenger_NotFound() {
        when(passengerRepo.findPassengerById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> passengerService.findPassenger(1L));
        assertEquals(String.format("Пассажир с ID: %d не существует", 1L), exception.getMessage());
    }


    @Test
    void registerPassenger_Success() {
        PassengerRequest request = new PassengerRequest("John Doe", "john.doe@example.com", "1234567890", "password123");
        when(passengerMapper.toEntity(request)).thenReturn(passenger);
        when(passengerRepo.findPassengerByGmail(request.gmail())).thenReturn(Optional.empty());
        when(passengerRepo.findPassengerByName(request.name())).thenReturn(Optional.empty());
        when(passengerRepo.findPassengerByPhoneNumber(request.phoneNumber())).thenReturn(Optional.empty());

        String response = passengerService.registerPassenger(request);

        assertEquals("АККАУНТ УСПЕШНО СОЗДАН!", response);
        verify(passengerRepo).save(passenger);
    }

    @Test
    void registerPassenger_EmailAlreadyExists() {
        PassengerRequest request = new PassengerRequest("John Doe", "john.doe@example.com", "1234567890", "password123");
        when(passengerRepo.findPassengerByGmail(request.gmail())).thenReturn(Optional.of(passenger));

        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class, () -> passengerService.registerPassenger(request));
        assertEquals(String.format("Пассажир с email %s уже зарегистрирован.", passenger.getGmail()), exception.getMessage());
    }

    @Test
    void loginPassenger_Success() {
        LoginPassengerRequest request = new LoginPassengerRequest("john.doe@example.com", "password123");
        when(passengerRepo.findPassengerByGmail(request.gmail())).thenReturn(Optional.of(passenger));
        when(passengerMapper.toDto(passenger)).thenReturn(passengerDto);

        PassengerDto result = passengerService.loginPassenger(request);

        assertEquals(passengerDto, result);
    }

    @Test
    void loginPassenger_InvalidCredentials() {
        LoginPassengerRequest request = new LoginPassengerRequest("john.doe@example.com", "wrongpassword");
        when(passengerRepo.findPassengerByGmail(request.gmail())).thenReturn(Optional.of(passenger));

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> passengerService.loginPassenger(request));
        assertEquals("Неверное имя пользователя или пароль.", exception.getMessage());
    }

    @Test
    void deletePassenger_Success() {
        mockPassengerExists();

        passengerService.deletePassenger(1L);

        verify(passengerRepo).delete(passenger);
    }

    @Test
    void deletePassenger_NotFound() {
        when(passengerRepo.findPassengerById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> passengerService.deletePassenger(1L));
        assertEquals("Пользователь не найден.", exception.getMessage());
    }

    @Test
    void updatePassengerRating_Success() {
        PassengerRatingEvent event = new PassengerRatingEvent(1L, 4.5F);
        mockPassengerExists();

        passengerService.updatePassengerRating(event);

        assertEquals(4.5F, passenger.getRating());
        verify(passengerRepo).save(passenger);
    }

    @Test
    void updatePassengerRating_NotFound() {
        PassengerRatingEvent event = new PassengerRatingEvent(1L, 4.5F);
        when(passengerRepo.findPassengerById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> passengerService.updatePassengerRating(event));
        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void updatePassengerRating_NullRating() {
        PassengerRatingEvent event = new PassengerRatingEvent(1L, null);
        mockPassengerExists();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> passengerService.updatePassengerRating(event));
        assertEquals("Rating cannot be null", exception.getMessage());
    }
}
