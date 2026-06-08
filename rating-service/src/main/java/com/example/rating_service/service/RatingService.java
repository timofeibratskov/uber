package com.example.rating_service.service;

import com.example.rating_service.model.dto.RatingRequestDto;
import com.example.rating_service.model.entity.RatingEntity;
import com.example.rating_service.model.entity.UserRatingEntity;
import com.example.rating_service.repo.RatingRepo;
import com.example.rating_service.repo.UserRatingRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepo ratingRepo;
    private final UserRatingRepo userRatingRepo;

    public String rateUser(RatingRequestDto request) {
        var userRatingEntity = userRatingRepo.findByTargetUserId(request.targetUserId())
                .orElse(
                        UserRatingEntity
                                .builder()
                                .averageRating(BigDecimal.ZERO)
                                .ratingSum(BigDecimal.ZERO)
                                .ratingCount(0L)
                                .targetUserId(request.targetUserId())
                                .build()
                );

        ratingRepo.save(RatingEntity.builder()
                .rideId(request.rideId())
                .raterUserId(request.raterUserId())
                .targetUserId(request.targetUserId())
                .rating(request.rating())
                .build());

        BigDecimal updatedSum = userRatingEntity.getRatingSum().add(BigDecimal.valueOf(request.rating()));

        long updatedCount = userRatingEntity.getRatingCount() + 1;

        BigDecimal newAvgRating = updatedSum.divide(BigDecimal.valueOf(updatedCount), 2, RoundingMode.HALF_UP);

        userRatingEntity.setAverageRating(newAvgRating);
        userRatingEntity.setRatingSum(updatedSum);
        userRatingEntity.setRatingCount(updatedCount);

        userRatingRepo.save(userRatingEntity);

        return "рейтинг доблавлен!";
    }

}
