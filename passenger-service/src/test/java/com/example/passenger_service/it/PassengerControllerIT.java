package com.example.passenger_service.it;

import com.example.passenger_service.exception.models.ErrorResponse;
import com.example.passenger_service.exception.models.ValidationErrorResponse;
import com.example.passenger_service.model.dto.LoginPassengerDto;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.RegisterPassengerDto;
import com.example.passenger_service.model.dto.UpdatePassengerDto;
import com.example.passenger_service.model.entity.PassengerEntity;
import com.example.passenger_service.model.enums.Gender;
import com.example.passenger_service.repo.PassengerRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PassengerControllerIT extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PassengerRepo passengerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        assertNotNull(response.getBody());
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
        assertNotNull(response.getBody());
        assertThat(response.getBody().getMessage()).isEqualTo("One or more fields are invalid");
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().getErrors().getFirst().getField()).isEqualTo("email");
        assertThat(response.getBody().getErrors().getFirst().getMessage()).isEqualTo("Почта в неверном формате!");
    }


    @Test
    @DisplayName("Успешный логин")
    void loginPassenger_Success() {
        // arrange
        String rawPassword = "securePass123";
        passengerRepo.save(PassengerEntity.builder()
                .id(UUID.randomUUID())
                .name("John")
                .email("login@gmail.com")
                .password(passwordEncoder.encode(rawPassword))
                .phoneNumber("+375291112233")
                .gender(Gender.MALE)
                .build());

        var loginRequest = new LoginPassengerDto("login@gmail.com", rawPassword);

        // act
        var response = restTemplate.postForEntity("/api/v1/passengers/login",
                loginRequest,
                PassengerResponseDto.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo("login@gmail.com");
    }

    @Test
    @DisplayName("Ошибка логина: неверный пароль")
    void loginPassenger_whenPasswordInvalid_ThrowUnauthorized() {
        // arrange
        passengerRepo.save(PassengerEntity.builder()
                .id(UUID.randomUUID())
                .name("John")
                .email("john@gmail.com")
                .password(passwordEncoder.encode("correct_pass"))
                .gender(Gender.MALE)
                .phoneNumber("+3752567566799")
                .build());

        var loginRequest = new LoginPassengerDto("wrong-pass@gmail.com", "incorrect_pass");

        // act
        var response = restTemplate.postForEntity("/api/v1/passengers/login",
                loginRequest,
                ErrorResponse.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertNotNull(response.getBody());
        assertThat(response.getBody().getMessage()).isEqualTo("Incorrect email or password!");
    }

    @Test
    @DisplayName("Ошибка логина: пустой email")
    void loginPassenger_whenRequestInvalid_ThrowValidationError() {
        // arrange
        var loginRequest = new LoginPassengerDto("", "somePass");

        // act
        var response = restTemplate.postForEntity("/api/v1/passengers/login",
                loginRequest,
                ValidationErrorResponse.class);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertNotNull(response.getBody());
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    @DisplayName("успешный поиск по id")
    void findPassengerById_Success() {
        // arrange
        UUID id = UUID.randomUUID();
        passengerRepo.save(PassengerEntity.builder()
                .id(id)
                .name("John")
                .email("john@gmail.com")
                .password(passwordEncoder.encode("correct_pass"))
                .gender(Gender.MALE)
                .phoneNumber("+3752567566799")
                .build());

        // act
        var response = restTemplate.getForEntity(
                "/api/v1/passengers/{id}",
                PassengerResponseDto.class,
                id);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
    }

    @Test
    @DisplayName("Ошибка поиска: нет id")
    void findPassengerById_whenIdNotExists_ThrowException() {
        // arrange
        UUID id = UUID.randomUUID();

        // act
        var response = restTemplate.getForEntity(
                "/api/v1/passengers/{id}",
                ErrorResponse.class,
                id);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertNotNull(response.getBody());
        assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("Успешное частичное обновление данных пассажира")
    void updatePassenger_Success() {
        // arrange
        UUID id = UUID.randomUUID();
        passengerRepo.save(PassengerEntity.builder()
                .id(id)
                .name("Old Name")
                .email("update@gmail.com")
                .password("pass123")
                .phoneNumber("+375291111111")
                .gender(Gender.MALE)
                .build());

        var request = UpdatePassengerDto.builder()
                .name("New Name")
                .phoneNumber("+375292222222")
                .gender(Gender.MALE)
                .build();

        // act
        var response = restTemplate.exchange(
                "/api/v1/passengers/{id}",
                HttpMethod.PATCH,
                new HttpEntity<>(request),
                PassengerResponseDto.class,
                id
        );

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("New Name");

        var updatedInDb = passengerRepo.findById(id).orElseThrow();
        assertThat(updatedInDb.getPhoneNumber()).isEqualTo("+375292222222");
    }

    @Test
    @DisplayName("Ошибка обновления: номер телефона уже занят")
    void updatePassenger_whenPhoneExists_ReturnsConflict() {
        // arrange
        UUID targetId = UUID.randomUUID();
        passengerRepo.save(PassengerEntity.builder()
                .id(targetId)
                .name("Target")
                .email("t@mail.com")
                .password("p")
                .phoneNumber("+375291111111")
                .build());

        passengerRepo.save(PassengerEntity.builder()
                .id(UUID.randomUUID())
                .name("Other")
                .email("o@mail.com")
                .password("p")
                .phoneNumber("+375299999999")
                .build());

        var request = UpdatePassengerDto.builder()
                .phoneNumber("+375299999999")
                .build();

        // act
        var response = restTemplate.exchange(
                "/api/v1/passengers/{id}",
                HttpMethod.PATCH,
                new HttpEntity<>(request),
                ErrorResponse.class,
                targetId
        );

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("CONFLICT");
    }

    @Test
    @DisplayName("Ошибка обновления: id не существует")
    void updatePassenger_whenIdNotExists_ReturnsConflict() {
        // arrange
        UUID targetId = UUID.randomUUID();

        var request = UpdatePassengerDto.builder()
                .phoneNumber("+375299999999")
                .build();

        // act
        var response = restTemplate.exchange(
                "/api/v1/passengers/{id}",
                HttpMethod.PATCH,
                new HttpEntity<>(request),
                ErrorResponse.class,
                targetId
        );

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
    }
}