package com.example.passenger_service.it;

import com.example.passenger_service.exception.models.ErrorResponse;
import com.example.passenger_service.exception.models.ValidationErrorResponse;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.RegisterPassengerDto;
import com.example.passenger_service.model.entity.PassengerEntity;
import com.example.passenger_service.model.enums.Gender;
import com.example.passenger_service.repo.PassengerRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PassengerControllerIT extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PassengerRepo passengerRepo;

    @BeforeEach
    void setUp() {
        passengerRepo.deleteAll();
    }

    @Test
    @DisplayName("Успешная регистрация нового пассажира")
    void registerPassenger_Success() {
        // arrange
        var request = RegisterPassengerDto.builder()
                .name("John")
                .email("john@gmail.com")
                .password("securePass123")
                .phoneNumber("+375291112233")
                .gender(Gender.MALE)
                .build();

        // act
        var response = restTemplate.postForEntity(
                "/api/v1/passengers/register",
                request,
                PassengerResponseDto.class
        );

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo("john@gmail.com");

        assertThat(passengerRepo.existsByEmail("john@gmail.com")).isTrue();
    }

    @Test
    @DisplayName("Ошибка регистрации: Email уже существует")
    void registerPassenger_whenEmailAlreadyExists_ThrowAlreadyExistsException() {
        // arrange
        passengerRepo.save(
                PassengerEntity.builder()
                        .id(UUID.randomUUID())
                        .name("John")
                        .email("john@gmail.com")
                        .password("securePass123")
                        .phoneNumber("+375291112233")
                        .gender(Gender.MALE)
                        .build()
        );

        var request = RegisterPassengerDto.builder()
                .name("John")
                .email("john@gmail.com")
                .password("securePass123")
                .phoneNumber("+375291112233")
                .gender(Gender.MALE)
                .build();

        // act
        var response = restTemplate.postForEntity(
                "/api/v1/passengers/register",
                request,
                ErrorResponse.class
        );

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getMessage()).isEqualTo("Email already exists!");
        assertThat(response.getBody().getCode()).isEqualTo("CONFLICT");
    }

    @Test
    @DisplayName("Ошибка регистрации: поле Email не соответстует шаблону")
    void registerPassenger_whenEmailIsNotValid_ThrowValidationErrorException() {
        // arrange
        var request = RegisterPassengerDto.builder()
                .name("John")
                .email("john")
                .password("securePass123")
                .phoneNumber("+375291112233")
                .gender(Gender.MALE)
                .build();

        // act
        var response = restTemplate.postForEntity(
                "/api/v1/passengers/register",
                request,
                ValidationErrorResponse.class
        );

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getMessage()).isEqualTo("One or more fields are invalid");
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().getErrors().getFirst().getField()).isEqualTo("email");
        assertThat(response.getBody().getErrors().getFirst().getMessage()).isEqualTo("Почта в неверном формате!");
    }
}