package com.example.rating_service.controller

import com.example.rating_service.dto.RatingRequestDto
import com.example.rating_service.dto.RatingResponseDto
import com.example.rating_service.dto.UserRatingSummaryDto
import com.example.rating_service.entity.RatingEntity
import com.example.rating_service.service.RatingService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/ratings")
class RatingController(private val ratingService: RatingService) {

    /**
     * {
     *   "rating": 4.5,
     *   "description": "Хорошее обслуживание",
     *   "senderId": 101,
     *   "recipientId": 202
     * }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addRating(@RequestBody ratingRequestDto: RatingRequestDto): RatingResponseDto {
        val ratingEntity = RatingEntity(
            id = 0L,
            rating = ratingRequestDto.rating,
            description = ratingRequestDto.description,
            senderId = ratingRequestDto.senderId,
            recipientId = ratingRequestDto.recipientId,
            createdAt = Instant.now()
        )
        ratingService.addRating(ratingEntity)
        val roundedRating = (Math.round(ratingEntity.rating * 10)) / 10f
        return RatingResponseDto(
            id = ratingEntity.id,
            rating = roundedRating,
            description = ratingEntity.description,
            senderId = ratingEntity.senderId,
            recipientId = ratingEntity.recipientId,
            createdAt = ratingEntity.createdAt
        )
    }

    /**
     * Возвращает список оценок для пользователя.
     */
    @GetMapping("/user/{recipientId}")
    fun getRatingsForUser(@PathVariable recipientId: Long): List<RatingResponseDto> {
        val ratings = ratingService.getRatingsForUser(recipientId)
        return ratings.map { rating ->
            RatingResponseDto(
                id = rating.id,
                rating = (Math.round(rating.rating * 10)) / 10f,
                description = rating.description,
                senderId = rating.senderId,
                recipientId = rating.recipientId,
                createdAt = rating.createdAt
            )
        }
    }


    @GetMapping("/user/{recipientId}/summary")
    fun getUserRatingSummary(@PathVariable recipientId: Long): UserRatingSummaryDto {
        val userRating = ratingService.getUserRatingSummary(recipientId)
        val roundedTotalRating = (Math.round(userRating.totalRating * 10)) / 10f
        return UserRatingSummaryDto(
            recipientId = userRating.recipientId,
            totalRating = roundedTotalRating,
            quantityRating = userRating.quantityRating
        )
    }
}
