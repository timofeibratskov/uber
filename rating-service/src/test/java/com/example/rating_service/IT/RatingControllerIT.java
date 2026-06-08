package com.example.rating_service.IT;

import com.example.rating_service.model.dto.RatingRequestDto;
import com.example.rating_service.repo.RatingRepo;
import com.example.rating_service.repo.UserRatingRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                .body(request)
                .exchange();

        //assert
        assertNotNull(result);

        result.expectStatus().isOk();

        var savedUser = userRatingRepo.findByTargetUserId(request.targetUserId()).orElseThrow();

        assertNotNull(savedUser);

        assertEquals(0, savedUser.getAverageRating().compareTo(BigDecimal.valueOf(request.rating())));
        assertEquals(0, savedUser.getRatingSum().compareTo(BigDecimal.valueOf(request.rating())));
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
}
