package com.example.rating_service.service;

import com.example.rating_service.exception.EntityNotFoundException;
import com.example.rating_service.model.dto.RatingRequestDto;
import com.example.rating_service.model.dto.UserRatingResponseDto;
import com.example.rating_service.model.entity.RatingEntity;
import com.example.rating_service.model.entity.UserRatingEntity;
import com.example.rating_service.repo.RatingRepo;
import com.example.rating_service.repo.UserRatingRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepo ratingRepo;
    private final UserRatingRepo userRatingRepo;

    @Transactional
    public void createUser(UUID userId) {
        if (userRatingRepo.findByTargetUserId(userId).isPresent()) {
            log.info("User with id {} already exists. Skipping creation.", userId);
            return;
        }

        var newUser = UserRatingEntity.builder()
                .averageRating(BigDecimal.ZERO)
                .ratingSum(0L)
                .ratingCount(0L)
                .targetUserId(userId)
                .build();

        userRatingRepo.save(newUser);
        log.info("Successfully created rating profile for user: {}", userId);
    }


    @Transactional
    public String rateUser(RatingRequestDto request) {
        var userRatingEntity = userRatingRepo.findByTargetUserId(request.targetUserId())
                .orElseThrow(() ->
                        new EntityNotFoundException("User rating profile not found for id: " + request.targetUserId()));

        ratingRepo.save(RatingEntity.builder()
                .rideId(request.rideId())
                .raterUserId(request.raterUserId())
                .targetUserId(request.targetUserId())
                .rating(request.rating())
                .build());

        var updatedSum = userRatingEntity.getRatingSum() + request.rating();

        long updatedCount = userRatingEntity.getRatingCount() + 1;
        BigDecimal average = BigDecimal.valueOf(updatedSum)
                .divide(BigDecimal.valueOf(updatedCount), 2, RoundingMode.HALF_UP);

        userRatingEntity.setAverageRating(average);
        userRatingEntity.setRatingSum(updatedSum);
        userRatingEntity.setRatingCount(updatedCount);

        userRatingRepo.save(userRatingEntity);

        return "рейтинг добавлен!";
    }

    @Transactional(readOnly = true)
    public UserRatingResponseDto getUserRating(UUID userId) {
        return UserRatingResponseDto.builder()
                .rating(userRatingRepo.findByTargetUserId(userId)
                        .orElseThrow(() ->
                                new EntityNotFoundException("user not found!")
                        )
                        .getAverageRating()
                )
                .build();
    }
}
