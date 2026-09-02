package com.example.passenger_service.it;

import com.example.passenger_service.client.RatingServiceClient;
import com.example.passenger_service.exception.models.ErrorResponse;
import com.example.passenger_service.model.dto.CompleteProfileRequestDto;
import com.example.passenger_service.model.dto.FavoriteAddressRequestDto;
import com.example.passenger_service.model.dto.FavoriteAddressResponseDto;
import com.example.passenger_service.model.dto.PassengerRatingResponse;
import com.example.passenger_service.model.dto.PassengerResponseDto;
import com.example.passenger_service.model.dto.UpdatePassengerDto;
import com.example.passenger_service.model.entity.FavoriteAddressEntity;
import com.example.passenger_service.model.entity.PassengerEntity;
import com.example.passenger_service.model.enums.Gender;
import com.example.passenger_service.repo.FavoriteAddressRepo;
import com.example.passenger_service.repo.PassengerRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class PassengerControllerIT extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PassengerRepo passengerRepo;

    @Autowired
    private FavoriteAddressRepo favoriteAddressRepo;

    @Mock
    private RatingServiceClient ratingServiceClient;

    @BeforeEach
    void setUp() {
        favoriteAddressRepo.deleteAll();
        passengerRepo.deleteAll();
    }

    @Test
    @DisplayName("Успешное сохранение профиля нового пассажира")
    void completePassengerProfile_Success() {
        // arrange
        var entity = passengerRepo.save(PassengerEntity.builder()
                .email("email")
                .build());

        var request = CompleteProfileRequestDto.builder()
                .name("John")
                .phoneNumber("+375291112233")
                .gender(Gender.MALE)
                .build();

        // act
        var response = restTemplate.postForEntity(
                "/api/v1/passengers/{id}/profile",
                request,
                String.class,
                entity.getId()
        );

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        var savedEntity = passengerRepo.findById(entity.getId());
        assertThat(savedEntity.isPresent()).isTrue();
        assertEquals(savedEntity.get().getName(), request.name());
        assertEquals(savedEntity.get().getGender(), request.gender());
        assertEquals(savedEntity.get().getPhoneNumber(), request.phoneNumber());
    }

    @Test
    @DisplayName("успешный поиск по id")
    void findPassengerById_Success() {
        // arrange
        var id = passengerRepo.save(PassengerEntity.builder()
                        .name("John")
                        .email("john@gmail.com")
                        .gender(Gender.MALE)
                        .phoneNumber("+3752567566799")
                        .build())
                .getId();

        var mockRatingResponse = new PassengerRatingResponse(new BigDecimal("4.85"));

        when(ratingServiceClient.getUserRating(id))
                .thenReturn(ResponseEntity.ok(mockRatingResponse));
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
        var id = passengerRepo.save(PassengerEntity.builder()
                        .name("Old Name")
                        .email("update@gmail.com")
                        .phoneNumber("+375291111111")
                        .gender(Gender.MALE)
                        .build())
                .getId();

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
                Void.class,
                id
        );

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var updatedInDb = passengerRepo.findById(id).orElseThrow();
        assertThat(updatedInDb.getName()).isEqualTo("New Name");
        assertThat(updatedInDb.getUpdatedAt()).isNotNull();
        assertThat(updatedInDb.getPhoneNumber()).isEqualTo("+375292222222");
    }

    @Test
    @DisplayName("Ошибка обновления: номер телефона уже занят")
    void updatePassenger_whenPhoneExists_ReturnsConflict() {
        // arrange
        UUID targetId = passengerRepo.save(PassengerEntity.builder()
                        .name("Target")
                        .email("t@mail.com")
                        .phoneNumber("+375291111111")
                        .build())
                .getId();

        passengerRepo.save(PassengerEntity.builder()
                .name("Other")
                .email("o@mail.com")
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

    @Test
    @DisplayName("Успешное добавление адреса")
    void deleteFavoriteAddress_ShouldReturn201AndSavedAddress() {
        // Arrange
        PassengerEntity passenger = PassengerEntity.builder()
                .name("john")
                .email("tim@example.com")
                .phoneNumber("+375291234567")
                .gender(Gender.MALE)
                .build();
        var savedPassenger = passengerRepo.save(passenger);

        UUID passengerId = savedPassenger.getId();
        FavoriteAddressRequestDto request = FavoriteAddressRequestDto.builder()
                .label("Home")
                .address("Grodno, Sovetskaya 1")
                .latitude(53.67)
                .longitude(23.83)
                .build();

        // Act
        ResponseEntity<FavoriteAddressResponseDto> response = restTemplate.postForEntity(
                "/api/v1/passengers/" + passengerId + "/addresses",
                request,
                FavoriteAddressResponseDto.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().label()).isEqualTo("Home");

        List<FavoriteAddressEntity> allInDb = favoriteAddressRepo.findByPassengerId(passengerId);
        assertThat(allInDb).hasSize(1);
        assertThat(allInDb.getFirst().getAddress()).isEqualTo("Grodno, Sovetskaya 1");
    }

    @Test
    @DisplayName("Получение списка адресов пассажира")
    void getFavoriteAddress_ShouldReturnList() {
        // Arrange
        PassengerEntity passenger = PassengerEntity.builder()
                .name("john")
                .email("tim@example.com")
                .phoneNumber("+375291234567")
                .gender(Gender.MALE)
                .build();
        var savedPassenger = passengerRepo.save(passenger);

        UUID passengerId = savedPassenger.getId();

        FavoriteAddressEntity address = FavoriteAddressEntity.builder()
                .passengerId(passengerId)
                .label("Gym")
                .latitude(123123123.123)
                .longitude(123123123.123)
                .address("Grodno, Kosmonavtov 100")
                .build();

        favoriteAddressRepo.save(address);

        // Act
        ResponseEntity<List<FavoriteAddressResponseDto>> response = restTemplate.exchange(
                "/api/v1/passengers/" + passengerId + "/addresses",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().label()).isEqualTo("Gym");
    }

    @Test
    @DisplayName("Успешное удаление адреса")
    void deleteFavoriteAddress_ShouldReturn204() {
        // Arrange
        PassengerEntity passenger = PassengerEntity.builder()
                .name("john")
                .email("tim@example.com")
                .phoneNumber("+375291234567")
                .gender(Gender.MALE)
                .build();
        var savedPassenger = passengerRepo.save(passenger);

        UUID passengerId = savedPassenger.getId();
        FavoriteAddressEntity address = FavoriteAddressEntity.builder()
                .passengerId(passengerId)
                .label("To Delete")
                .latitude(123123123.123)
                .longitude(123123123.123)
                .address("Some Address")
                .build();
        FavoriteAddressEntity savedAddress = favoriteAddressRepo.save(address);
        UUID addressId = savedAddress.getId();

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/passengers/" + passengerId + "/addresses/" + addressId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(favoriteAddressRepo.findById(addressId)).isEmpty();
    }

    @Test
    @DisplayName("Ошибка добавления: попытка добавить 6-й адрес")
    void deleteFavoriteAddress_ShouldReturn400_WhenLimitExceeded() {
        // Arrange
        PassengerEntity passenger = PassengerEntity.builder()
                .name("john")
                .email("tim@example.com")
                .phoneNumber("+375291234567")
                .gender(Gender.MALE)
                .build();
        var savedPassenger = passengerRepo.save(passenger);

        UUID passengerId = savedPassenger.getId();

        for (int i = 1; i <= 5; i++) {
            FavoriteAddressEntity address = FavoriteAddressEntity.builder()
                    .passengerId(passengerId)
                    .label("Label " + i)
                    .address("Address " + i)
                    .latitude(53.0 + i)
                    .longitude(23.0 + i)
                    .build();
            favoriteAddressRepo.save(address);
        }

        FavoriteAddressRequestDto request6 = FavoriteAddressRequestDto.builder()
                .label("Sixth Address")
                .address("Grodno, Center")
                .latitude(53.9)
                .longitude(27.5)
                .build();

        // Act
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/passengers/" + passengerId + "/addresses",
                request6,
                ErrorResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("ADDRESS_LIMIT_EXCEEDED");
        assertThat(response.getBody().getMessage()).isEqualTo("favorite addresses is too much!");

        List<FavoriteAddressEntity> allInDb = favoriteAddressRepo.findByPassengerId(passengerId);
        assertThat(allInDb).hasSize(5);
    }
}