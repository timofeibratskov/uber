package com.example.rating_service.mapper

import com.example.rating_service.dto.RatingRequestDto
import com.example.rating_service.dto.RatingResponseDto
import com.example.rating_service.dto.UserRatingSummaryDto
import com.example.rating_service.entity.RatingEntity
import com.example.rating_service.entity.UserRatingEntity
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RatingMapper {
    fun toDto(entity: RatingEntity): RatingResponseDto {
        return RatingResponseDto(
            id = entity.id,
            rating = (Math.round(entity.rating * 10)) / 10f,
            description = entity.description,
            senderId = entity.senderId,
            recipientId = entity.recipientId,
            rideId = entity.rideId,
            createdAt = entity.createdAt,
            senderType = entity.senderType
        )
    }

    fun toEntity(request: RatingRequestDto): RatingEntity {
        return RatingEntity(
            rating = request.rating,
            description = request.description,
            senderId = request.senderId,
            recipientId = request.recipientId,
            createdAt = Instant.now(),
            rideId = request.rideId,
            senderType = request.senderType
        )
    }

    fun toRatingSummaryDto(userRatingEntity: UserRatingEntity): UserRatingSummaryDto {
        return UserRatingSummaryDto(
            recipientId = userRatingEntity.recipientId,
            totalRating = (Math.round(userRatingEntity.totalRating * 10)) / 10f,
            quantityRating = userRatingEntity.quantityRating,
        )
    }
}