package com.example.rating_service.service

import com.example.rating_service.dto.RatingResponseDto
import com.example.rating_service.dto.UserRatingSummaryDto
import com.example.rating_service.dto.UserRatingEvent
import com.example.rating_service.dto.RatingIdInRideEvent
import com.example.rating_service.dto.RatingRequestDto
import com.example.rating_service.entity.UserRatingEntity
import com.example.rating_service.enums.SenderType
import com.example.rating_service.exception.MissingRequiredFieldException
import com.example.rating_service.exception.NotFoundException
import com.example.rating_service.mapper.RatingMapper
import com.example.rating_service.repo.RatingRepo
import com.example.rating_service.repo.UserRatingRepo
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RatingService(
    private val ratingRepository: RatingRepo,
    private val userRatingRepository: UserRatingRepo,
    private val sequenceGeneratorService: SequenceGeneratorService,
    private val ratingMapper: RatingMapper,
    private val kafkaSenderToUser: KafkaTemplate<String, UserRatingEvent>,
    private val kafkaSenderToRide: KafkaTemplate<String, RatingIdInRideEvent>
) {

    @Transactional
    fun addRating(ratingRequestDto: RatingRequestDto): String {
        val ratingEntity = ratingMapper.toEntity(ratingRequestDto)

        if (ratingEntity.senderId <= 0 || ratingEntity.recipientId <= 0) {
            throw MissingRequiredFieldException("Sender ID and Recipient ID must be provided and greater than zero.")
        }
        if (ratingEntity.rating < 1 || ratingEntity.rating > 5) {
            throw MissingRequiredFieldException("Rating must be between 1 and 5.")
        }
        if (ratingEntity.rating < 3 && ratingEntity.description.isNullOrBlank()) {
            throw MissingRequiredFieldException("A description is required for ratings lower than 3.")
        }

        if (ratingEntity.id == 0L) {
            ratingEntity.id = sequenceGeneratorService.generateSequence("rating_sequence")
        }
        ratingRepository.save(ratingEntity)

        var userRating = userRatingRepository.findByRecipientId(ratingEntity.recipientId)
        if (userRating == null) {
            userRating = UserRatingEntity(
                id = sequenceGeneratorService.generateSequence("user_rating_sequence"),
                recipientId = ratingEntity.recipientId,
                totalRating = ratingEntity.rating,
                quantityRating = 1

            )
        } else {
            val newAvg = ((userRating.quantityRating * userRating.totalRating)
                    + ratingRequestDto.rating) /
                    (userRating.quantityRating + 1)
            userRating.totalRating = newAvg
            userRating.quantityRating++
        }
        userRatingRepository.save(userRating)
        println(userRating.recipientId)
        println(userRating.totalRating)

        val userEvent = UserRatingEvent(recipientId = userRating.recipientId, rating = userRating.totalRating)
        val rideEvent = RatingIdInRideEvent(
            rideId = ratingEntity.rideId,
            recipientRatingId = ratingEntity.id,
            type = ratingEntity.senderType
        )

        if (ratingEntity.senderType == SenderType.PASSENGER) {
            kafkaSenderToUser.send("DRIVER-rating-event", userEvent)
        } else {
            kafkaSenderToUser.send("PASSENGER-rating-event", userEvent)
        }
        kafkaSenderToRide.send("rating-id-event", rideEvent)
        return "добавлено"
    }

    fun getRatingsForUser(recipientId: Long): List<RatingResponseDto> {
        val ratings = ratingRepository.findByRecipientId(recipientId)
        if (ratings.isEmpty()) {
            throw NotFoundException("No ratings found for recipient with ID: $recipientId")
        }
        return ratings.map { rating -> ratingMapper.toDto(rating) }
    }

    fun getUserRatingSummary(recipientId: Long): UserRatingSummaryDto {
        val entity = userRatingRepository.findByRecipientId(recipientId)
            ?: throw NotFoundException("User rating summary not found for recipient with ID: $recipientId")

        val userRating = ratingMapper.toRatingSummaryDto(entity)
        return userRating
    }

    fun getRatingEntityById(id: Long): RatingResponseDto {
        val ratingEntity = ratingRepository.findById(id)
            .orElseThrow { NotFoundException("Рейтинг с ID $id не найден") }

        return ratingMapper.toDto(ratingEntity)
    }
}
