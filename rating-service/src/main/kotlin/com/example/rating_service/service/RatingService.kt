package com.example.rating_service.service

import com.example.rating_service.entity.RatingEntity
import com.example.rating_service.entity.UserRatingEntity
import com.example.rating_service.exception.MissingRequiredFieldException
import com.example.rating_service.exception.NotFoundException
import com.example.rating_service.repo.RatingRepo
import com.example.rating_service.repo.UserRatingRepo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RatingService(
    private val ratingRepository: RatingRepo,
    private val userRatingRepository: UserRatingRepo,
    private val sequenceGeneratorService: SequenceGeneratorService
) {

    @Transactional
    fun addRating(ratingEntity: RatingEntity) {
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
            val newAvg = ((userRating.quantityRating * userRating.totalRating) + ratingEntity.rating) /
                    (userRating.quantityRating + 1)
            userRating.totalRating = newAvg
            userRating.quantityRating++
        }
        userRatingRepository.save(userRating)
    }

    fun getRatingsForUser(recipientId: Long): List<RatingEntity> {
        val ratings = ratingRepository.findByRecipientId(recipientId)
        if (ratings.isEmpty()) {
            throw NotFoundException("No ratings found for recipient with ID: $recipientId")
        }
        return ratings
    }

    fun getUserRatingSummary(recipientId: Long): UserRatingEntity {
        return userRatingRepository.findByRecipientId(recipientId)
            ?: throw NotFoundException("User rating summary not found for recipient with ID: $recipientId")
    }
}
