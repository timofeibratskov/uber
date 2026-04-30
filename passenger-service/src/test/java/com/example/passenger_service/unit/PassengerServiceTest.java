package com.example.passenger_service.unit;

import com.example.passenger_service.exception.AlreadyExistsException;
import com.example.passenger_service.exception.InvalidCredentialsException;
import com.example.passenger_service.exception.PassengerNotFoundException;
import com.example.passenger_service.mapper.PassengerMapper;
import com.example.passenger_service.model.dto.LoginPassengerDto;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.RegisterPassengerDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
                BigDecimal.ZERO,
                Gender.MALE
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

        verify(passengerRepo, times(1)).save(any());
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

    @Test
    @DisplayName("Успешный логин пассажира")
    public void loginPassenger_Success() {
        // arrange
        UUID id = UUID.randomUUID();
        String phoneNumber = "+375295875657";
        String email = "johnDoe228@gmail.com";
        String password = "password";
        Gender gender = Gender.MALE;
        String name = "john";
        BigDecimal rating = BigDecimal.ZERO;

        var request = LoginPassengerDto.builder()
                .email(email)
                .password(password)
                .build();

        var entity = PassengerEntity.builder()
                .id(id)
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber)
                .rating(rating)
                .gender(gender)
                .build();

        var response = PassengerResponseDto.builder()
                .id(id)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .rating(rating)
                .build();

        when(passengerRepo.findByEmail(request.email())).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches(request.password(), entity.getPassword())).thenReturn(true);
        when(passengerMapper.toResponseDto(entity)).thenReturn(response);

        // act
        var result = passengerService.loginPassenger(request);

        // assert
        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals(email, result.email());
        assertEquals(name, result.name());

        verify(passengerRepo).findByEmail(email);
        verify(passwordEncoder).matches(request.password(), entity.getPassword());
    }

    @Test
    @DisplayName("Ошибка логина: неверный пароль")
    public void loginPassenger_WrongPassword_ThrowsException() {
        // arrange
        UUID id = UUID.randomUUID();
        String phoneNumber = "+375295875657";
        String email = "johnDoe228@gmail.com";
        String correctPassword = "encoded_password";
        String wrongPassword = "wrong pass";
        Gender gender = Gender.MALE;
        String name = "john";
        BigDecimal rating = BigDecimal.ZERO;

        var request = LoginPassengerDto.builder()
                .email(email)
                .password("wrong pass")
                .build();

        var entity = PassengerEntity.builder()
                .id(id)
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(correctPassword))
                .phoneNumber(phoneNumber)
                .rating(rating)
                .gender(gender)
                .build();

        when(passengerRepo.findByEmail(email)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches(correctPassword, entity.getPassword())).thenReturn(false);

        // act
        var exception = assertThrows(InvalidCredentialsException.class, () ->
                passengerService.loginPassenger(request)
        );

        // assert
        assertEquals("Incorrect email or password!", exception.getMessage());
        verify(passengerRepo, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(wrongPassword, entity.getPassword());
        verifyNoInteractions(passengerMapper);
    }

    @Test
    @DisplayName("Ошибка логина: почты нет")
    public void loginPassenger_EmailNotExists_ThrowsException() {
        // arrange
        String email = "notfound@gmail.com";

        var request = LoginPassengerDto.builder()
                .email(email)
                .password("wrong pass")
                .build();

        when(passengerRepo.findByEmail(email)).thenReturn(Optional.empty());

        // act
        var exception = assertThrows(InvalidCredentialsException.class, () ->
                passengerService.loginPassenger(request)
        );

        // assert
        assertEquals("Incorrect email or password!", exception.getMessage());
        verify(passengerRepo, times(1)).findByEmail(email);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(passengerMapper);
    }

    @Test
    @DisplayName("Успешный поиск пассажира по id")
    public void findPassengerById_Success() {
        // arrange
        UUID id = UUID.randomUUID();

        var entity = PassengerEntity.builder()
                .id(id)
                .name("john")
                .email("johnDoe228@gmail.com")
                .password("encoded_password")
                .phoneNumber("+375295875657")
                .gender(Gender.MALE)
                .rating(BigDecimal.ZERO)
                .build();

        var response = PassengerResponseDto.builder()
                .id(id)
                .name(entity.getName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .rating(entity.getRating())
                .build();

        when(passengerRepo.findById(id)).thenReturn(Optional.of(entity));
        when(passengerMapper.toResponseDto(entity)).thenReturn(response);

        // act
        var result = passengerService.findPassengerById(id);

        // assert
        assertNotNull(result);
        assertEquals(id, result.id());
        verify(passengerRepo, times(1)).findById(id);
        verify(passengerMapper, times(1)).toResponseDto(entity);
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
    @DisplayName("Успешное обновление пассажира")
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

        var responseDto = PassengerResponseDto.builder()
                .id(id)
                .name("New Name")
                .phoneNumber("+375291112233")
                .gender(Gender.MALE)
                .email("name@gmail.com")
                .rating(BigDecimal.ZERO)
                .build();

        when(passengerRepo.findById(id)).thenReturn(Optional.of(entity));
        when(passengerRepo.existsByPhoneNumber(request.phoneNumber())).thenReturn(false);
        when(passengerMapper.toResponseDto(entity)).thenReturn(responseDto);

        // act
        var result = passengerService.updatePassenger(id, request);

        // assert
        assertEquals("New Name", result.name());
        assertEquals(Gender.MALE, result.gender());
        assertEquals(request.phoneNumber(), result.phoneNumber());
        verify(passengerRepo).findById(id);
        verify(passengerRepo, times(1)).existsByPhoneNumber(request.phoneNumber());
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
        verifyNoInteractions(passengerMapper);
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
        verifyNoInteractions(passengerMapper);
    }
}