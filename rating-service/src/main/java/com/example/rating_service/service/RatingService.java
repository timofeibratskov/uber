package com.example.rating_service.service;

import com.example.rating_service.exception.EntityNotFoundException;
import com.example.rating_service.model.dto.RatingRequestDto;
import com.example.rating_service.model.dto.UserRatingResponseDto;
import com.example.rating_service.model.entity.RatingEntity;
import com.example.rating_service.model.entity.UserRatingEntity;
import com.example.rating_service.repo.RatingRepo;
import com.example.rating_service.repo.UserRatingRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepo ratingRepo;
    private final UserRatingRepo userRatingRepo;

    //todo исправить (проверка наличия такого пользователя перед созданием )
    public String rateUser(RatingRequestDto request) {
        var userRatingEntity = userRatingRepo.findByTargetUserId(request.targetUserId())
                .orElse(
                        UserRatingEntity
                                .builder()
                                .averageRating(BigDecimal.ZERO)
                                .ratingSum(0L)
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

        var updatedSum = userRatingEntity.getRatingSum() + request.rating();

        long updatedCount = userRatingEntity.getRatingCount() + 1;

        userRatingEntity.setAverageRating(BigDecimal.valueOf(updatedSum / updatedCount));
        userRatingEntity.setRatingSum(updatedSum);
        userRatingEntity.setRatingCount(updatedCount);

        userRatingRepo.save(userRatingEntity);

        return "рейтинг добавлен!";
    }

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
