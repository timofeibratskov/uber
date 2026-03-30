package com.example.passenger_service.unit;

import com.example.passenger_service.exception.AlreadyExistsException;
import com.example.passenger_service.mapper.PassengerMapper;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.RegisterPassengerDto;
import com.example.passenger_service.model.entity.PassengerEntity;
import com.example.passenger_service.model.enums.Gender;
import com.example.passenger_service.repo.PassengerRepo;
import com.example.passenger_service.service.PassengerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class PassengerServiceTest {
    @Mock
    private PassengerMapper passengerMapper;

    @Mock
    private PassengerRepo passengerRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PassengerService passengerService;

    @Test
    @DisplayName("Успешная регистрация пассажира")
    public void registerPassenger_Success() {
        // arrange
        var request = RegisterPassengerDto.builder()
                .name("john")
                .email("johnDoe228@gmail.com")
                .password("superPassword1")
                .phoneNumber("+375295875657")
                .gender(Gender.MALE)
                .build();

        var passengerEntity = PassengerEntity.builder()
                .id(UUID.randomUUID())
                .name("john")
                .email("johnDoe228@gmail.com")
                .password("encoded_password")
                .phoneNumber("+375295875657")
                .gender(Gender.MALE)
                .build();

        var responseDto = new PassengerResponseDto(
                passengerEntity.getId(),
                "john",
                "johnDoe228@gmail.com",
                "+375295875657",
                java.math.BigDecimal.ZERO
        );

        when(passengerRepo.existsByEmail(request.email())).thenReturn(false);
        when(passengerRepo.existsByPhoneNumber(request.phoneNumber())).thenReturn(false);
        when(passengerMapper.toEntity(request)).thenReturn(new PassengerEntity());
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");

        when(passengerRepo.save(any(PassengerEntity.class))).thenReturn(passengerEntity);
        when(passengerMapper.toResponseDto(any(PassengerEntity.class))).thenReturn(responseDto);

        // act
        var result = passengerService.registerPassenger(request);

        // assert
        assertNotNull(result);
        assertEquals(request.email(), result.email());
        assertEquals(java.math.BigDecimal.ZERO, result.rating());

        verify(passengerRepo, org.mockito.Mockito.times(1)).save(any());
    }

    @Test
    @DisplayName("Ошибка регистрации: Email уже существует")
    public void registerPassenger_whenEmailExist_ThrowAlreadyExistsException() {
        // arrange
        var request = RegisterPassengerDto.builder()
                .name("john")
                .email("johnDoe228@gmail.com")
                .password("superPassword1")
                .phoneNumber("+375295875657")
                .gender(Gender.MALE)
                .build();

        when(passengerRepo.existsByEmail(request.email())).thenReturn(true);

        // act
        var exception = assertThrows(
                AlreadyExistsException.class,
                () -> passengerService.registerPassenger(request)
        );

        // assert
        assertEquals("Email already exists!", exception.getMessage());
        verify(passengerRepo, never()).save(any());
        verify(passengerRepo, never()).existsByPhoneNumber(any());
    }

    @Test
    @DisplayName("Ошибка регистрации: номер телефона уже существует")
    public void registerPassenger_whenPhoneNumberExist_ThrowAlreadyExistsException() {
        // arrange
        var request = RegisterPassengerDto.builder()
                .name("john")
                .email("johnDoe228@gmail.com")
                .password("superPassword1")
                .phoneNumber("+375295875657")
                .gender(Gender.MALE)
                .build();

        when(passengerRepo.existsByEmail(request.email())).thenReturn(false);
        when(passengerRepo.existsByPhoneNumber(request.phoneNumber())).thenReturn(true);

        // act
        var exception = assertThrows(
                AlreadyExistsException.class,
                () -> passengerService.registerPassenger(request)
        );

        // assert
        assertEquals("Phone number already exists!", exception.getMessage());
        verify(passengerRepo, never()).save(any());
    }
}