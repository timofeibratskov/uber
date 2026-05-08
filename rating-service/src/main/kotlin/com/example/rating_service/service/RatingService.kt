package com.example.rating_service.service

import com.example.rating_service.dto.RatingRequestDto
import com.example.rating_service.dto.RatingResponseDto
import com.example.rating_service.entity.RatingEntity
import com.example.rating_service.entity.UserRatingEntity
import com.example.rating_service.exception.UserAlreadyRatedException
import com.example.rating_service.repo.RatingRepo
import com.example.rating_service.repo.UserRatingRepo
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RatingService(
    private val ratingRepo: RatingRepo,
    private val userRatingRepo: UserRatingRepo
) {
    @Transactional
    fun addRating(request: RatingRequestDto): RatingResponseDto {
        if (ratingRepo.existsByRideId(request.rideId))
            throw UserAlreadyRatedException("Ride with id ${request.rideId} already rated")


        val userRating = userRatingRepo.findByUserId(request.targetUserId)
            ?: UserRatingEntity(
                UUID.randomUUID(),
                request.targetUserId,
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO
            )

        val newTotal = userRating.totalScore.add(request.rating)
        val newCount = userRating.count + 1
        val newRating = newTotal.divide(
            newCount.toBigDecimal(),
            2,
            RoundingMode.HALF_UP
        )

        ratingRepo.save(
            RatingEntity(
                id = UUID.randomUUID(),
                rideId = request.rideId,
                targetUserId = request.targetUserId,
                rating = request.rating
            )
        )

        userRating.totalScore = newTotal
        userRating.count = newCount
        userRating.rating = newRating
        userRatingRepo.save(userRating)

        return RatingResponseDto(
            message = "Rating received and will be processed",
            targetUserId = userRating.userId
        )
    }
}
