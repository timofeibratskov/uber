package com.example.rating_service.IT;

import com.example.rating_service.exception.models.ErrorResponse;
import com.example.rating_service.model.dto.RatingRequestDto;
import com.example.rating_service.model.dto.UserRatingResponseDto;
import com.example.rating_service.model.entity.UserRatingEntity;
import com.example.rating_service.repo.RatingRepo;
import com.example.rating_service.repo.UserRatingRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RatingControllerIT extends BaseIT {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private RatingRepo ratingRepo;

    @Autowired
    private UserRatingRepo userRatingRepo;


    @DisplayName("создание первого рейтинга пользователя")
    @Test
    void rateUser_WithValidRequest_ShouldReturnOk() throws Exception {
        // arrange
        var request = RatingRequestDto.builder()
                .rating(5)
                .raterUserId(UUID.randomUUID())
                .targetUserId(UUID.randomUUID())
                .rideId(UUID.randomUUID())
                .build();

        // act
        var result = restTestClient.post()
                .uri("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        //assert
        assertNotNull(result);

        result.expectStatus().isCreated();

        var savedUser = userRatingRepo.findByTargetUserId(request.targetUserId()).orElseThrow();

        assertNotNull(savedUser);

        assertEquals(0, savedUser.getAverageRating().compareTo(BigDecimal.valueOf(request.rating())));
        assertEquals(savedUser.getRatingSum(), (long) request.rating());
        assertEquals(1L, savedUser.getRatingCount());

        var savedRating = ratingRepo.findByTargetUserId(request.targetUserId()).orElseThrow();
        assertNotNull(savedRating);
        assertEquals(savedRating.getRating(), request.rating());
        assertEquals(savedRating.getRaterUserId(), request.raterUserId());
        assertEquals(savedRating.getTargetUserId(), request.targetUserId());
        assertEquals(savedRating.getRideId(), request.rideId());
        assertEquals(savedRating.getRating(), request.rating());
        assertNotNull(savedRating.getCreatedAt());
    }

    @DisplayName("Ошибка: дублирование рейтинга пользователя для одной поездки")
    @Test
    void rateUser_WithValidRequest_ShouldReturnAlreadyExistsException() throws Exception {
        // arrange
        var request = RatingRequestDto.builder()
                .rating(5)
                .raterUserId(UUID.randomUUID())
                .targetUserId(UUID.randomUUID())
                .rideId(UUID.randomUUID())
                .build();

        restTestClient.post()
                .uri("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchangeSuccessfully();

        // act
        var result = restTestClient.post()
                .uri("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(ErrorResponse.class)
                .returnResult();

        // assert
        ErrorResponse errorBody = result.getResponseBody();
        assertNotNull(errorBody);
        assertEquals("CONFLICT", errorBody.getCode());
        assertNotNull(errorBody.getMessage());
    }

    @DisplayName("успешный возврат рейтинга пользователя")
    @Test
    void getUserRating_whenUserExists_ShouldReturnOk() throws Exception {
        // arrange
        var userRating = userRatingRepo.save(UserRatingEntity.builder()
                .ratingCount(1L)
                .averageRating(BigDecimal.valueOf(5))
                .ratingSum(5L)
                .ratingCount(1L)
                .targetUserId(UUID.randomUUID())
                .build()
        );
        userRatingRepo.save(userRating);

        // act
        var result = restTestClient.get()
                .uri("/api/v1/ratings/users/{id}", userRating.getTargetUserId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserRatingResponseDto.class)
                .returnResult();


        //assert
        assertNotNull(result.getResponseBody());

        assertEquals(result.getResponseBody().rating(), userRating.getAverageRating());
    }

    @DisplayName("Ошибка получения рейтинга пользователя: пользователя не существует")
    @Test
    void getUserRating_whenUserNotExists_ShouldReturnEntityNotFoundException() throws Exception {

        // act
        var result = restTestClient.get()
                .uri("/api/v1/ratings/users/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .returnResult();


        //assert
        assertNotNull(result.getResponseBody());

        assertEquals("NOT_FOUND", result.getResponseBody().getCode());
        assertNotNull(result.getResponseBody().getMessage());
    }
}
