package com.example.passenger_service.unit;

import com.example.passenger_service.client.RatingServiceClient;
import com.example.passenger_service.exception.AlreadyExistsException;
import com.example.passenger_service.exception.PassengerNotFoundException;
import com.example.passenger_service.mapper.PassengerMapper;
import com.example.passenger_service.model.dto.CompleteProfileRequestDto;
import com.example.passenger_service.model.dto.PassengerRatingResponse;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.UpdatePassengerDto;
import com.example.passenger_service.model.entity.PassengerEntity;
import com.example.passenger_service.model.enums.Gender;
import com.example.passenger_service.repo.PassengerRepo;
import com.example.passenger_service.service.PassengerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class PassengerServiceTest {
    @Mock
    private PassengerMapper passengerMapper;

    @Mock
    private PassengerRepo passengerRepo;

    @Mock
    private RatingServiceClient ratingServiceClient;

    @InjectMocks
    private PassengerService passengerService;

    @Test
    @DisplayName("Успешное заполнение профиля пассажира")
    public void completeProfile_Success() {
        // arrange
        var request = CompleteProfileRequestDto.builder()
                .name("john")
                .phoneNumber("+375295875657")
                .gender(Gender.MALE)
                .build();

        var entity = PassengerEntity.builder()
                .id(UUID.randomUUID())
                .email("johnDoe228@gmail.com")
                .build();

        var savedEntity = PassengerEntity.builder()
                .id(UUID.randomUUID())
                .name("john")
                .email("johnDoe228@gmail.com")
                .phoneNumber("+375295875657")
                .gender(Gender.MALE)
                .build();

        when(passengerRepo.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(passengerRepo.existsByPhoneNumber(request.phoneNumber())).thenReturn(false);
        doNothing().when(passengerMapper).updateEntity(savedEntity, request);

        when(passengerRepo.save(any(PassengerEntity.class))).thenReturn(savedEntity);

        // act
        var result = passengerService.completeProfile(entity.getId(), request);

        // assert
        assertNotNull(result);

        verify(passengerRepo, times(1)).save(any());
    }


    @Test
    @DisplayName("Ошибка заполнения профиля: номер телефона уже существует")
    public void completeProfile_whenPhoneNumberExist_ThrowAlreadyExistsException() {
        // arrange
        var request = CompleteProfileRequestDto.builder()
                .name("john")
                .phoneNumber("+375295875657")
                .gender(Gender.MALE)
                .build();
        var entity = PassengerEntity.builder()
                .id(UUID.randomUUID())
                .email("johnDoe228@gmail.com")
                .build();
        when(passengerRepo.findById(any())).thenReturn(Optional.of(entity));
        when(passengerRepo.existsByPhoneNumber(request.phoneNumber())).thenReturn(true);

        // act
        var exception = assertThrows(
                AlreadyExistsException.class,
                () -> passengerService.completeProfile(any(), request)
        );

        // assert
        assertEquals("Phone number already exists!", exception.getMessage());
        verify(passengerRepo, never()).save(any());
    }


    @Test
    @DisplayName("Успешный поиск пассажира по id")
    public void findPassengerById_Success() {
        // arrange
        UUID id = UUID.randomUUID();
        BigDecimal rating = BigDecimal.valueOf(5);

        var entity = PassengerEntity.builder()
                .id(id)
                .name("john")
                .email("johnDoe228@gmail.com")
                .phoneNumber("+375295875657")
                .gender(Gender.MALE)
                .build();

        var response = PassengerResponseDto.builder()
                .id(id)
                .name(entity.getName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .rating(rating)
                .build();

        when(passengerRepo.findById(id)).thenReturn(Optional.of(entity));
        when(ratingServiceClient.getUserRating(id)).thenReturn(ResponseEntity.ok(PassengerRatingResponse.builder()
                        .rating(BigDecimal.valueOf(5))
                        .build()
                )
        );
        when(passengerMapper.toResponseDto(entity, rating)).thenReturn(response);

        // act
        var result = passengerService.findPassengerById(id);

        // assert
        assertNotNull(result);
        assertEquals(id, result.id());
        verify(passengerRepo, times(1)).findById(id);
        verify(passengerMapper, times(1)).toResponseDto(entity, rating);
    }

    @Test
    @DisplayName("Ошибка поиска: нет id")
    public void findPassengerById_whenIdNotExists_throwsException() {
        // arrange
        UUID id = UUID.randomUUID();

        // act
        var exception = assertThrows(PassengerNotFoundException.class, () ->
                passengerService.findPassengerById(id)
        );

        // assert
        assertEquals("Passenger not found!", exception.getMessage());
        verify(passengerRepo, times(1)).findById(id);
        verifyNoInteractions(passengerMapper);
    }

    @Test
    @DisplayName("Успешное обновление всех полей пассажира")
    void updatePassenger_Success() {
        // arrange
        UUID id = UUID.randomUUID();

        var request = UpdatePassengerDto.builder()
                .name("New Name")
                .phoneNumber("+375291112233")
                .gender(Gender.MALE)
                .build();

        var entity = PassengerEntity.builder()
                .id(id)
                .name("Old Name")
                .phoneNumber("+375290000000")
                .gender(Gender.FEMALE)
                .build();

        when(passengerRepo.findById(id)).thenReturn(Optional.of(entity));
        when(passengerRepo.existsByPhoneNumber(request.phoneNumber())).thenReturn(false);

        // act
        passengerService.updatePassenger(id, request);

        // assert
        verify(passengerRepo, times(1)).findById(id);
        verify(passengerRepo, times(1)).existsByPhoneNumber(request.phoneNumber());

        assertEquals("New Name", entity.getName());
        assertEquals("+375291112233", entity.getPhoneNumber());
        assertEquals(Gender.MALE, entity.getGender());
    }

    @Test
    @DisplayName("Обновление: конфликт номера телефона")
    void updatePassenger_whenPhoneExists_throwsException() {
        // arrange
        UUID id = UUID.randomUUID();

        var request = UpdatePassengerDto.builder()
                .phoneNumber("+375291112233")
                .build();

        var entity = PassengerEntity.builder()
                .id(id)
                .phoneNumber("+375290000000")
                .build();

        when(passengerRepo.findById(id)).thenReturn(Optional.of(entity));
        when(passengerRepo.existsByPhoneNumber(request.phoneNumber())).thenReturn(true);

        // act
        var exception = assertThrows(AlreadyExistsException.class, () ->
                passengerService.updatePassenger(id, request)
        );

        // assert
        assertTrue(exception.getMessage().contains(request.phoneNumber()));
        verify(passengerRepo).existsByPhoneNumber(anyString());
    }

    @Test
    @DisplayName("Обновление: нет id")
    void updatePassenger_whenIdNotExists_throwsException() {
        // arrange
        UUID id = UUID.randomUUID();

        var request = UpdatePassengerDto.builder()
                .phoneNumber("+375291112233")
                .build();

        when(passengerRepo.findById(id)).thenReturn(Optional.empty());

        // act
        var exception = assertThrows(PassengerNotFoundException.class, () ->
                passengerService.updatePassenger(id, request)
        );

        // assert
        assertTrue(exception.getMessage().contains("Passenger not found!"));
        verify(passengerRepo).findById(id);
    }
}