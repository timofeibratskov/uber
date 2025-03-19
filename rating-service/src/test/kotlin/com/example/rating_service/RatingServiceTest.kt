package com.example.rating_service

import com.example.rating_service.dto.UserRatingEvent
import com.example.rating_service.dto.RatingIdInRideEvent
import com.example.rating_service.dto.RatingRequestDto
import com.example.rating_service.dto.RatingResponseDto
import com.example.rating_service.dto.UserRatingSummaryDto
import com.example.rating_service.entity.RatingEntity
import com.example.rating_service.entity.UserRatingEntity
import com.example.rating_service.enums.SenderType
import com.example.rating_service.exception.MissingRequiredFieldException
import com.example.rating_service.exception.NotFoundException
import com.example.rating_service.mapper.RatingMapper
import com.example.rating_service.repo.RatingRepo
import com.example.rating_service.repo.UserRatingRepo
import com.example.rating_service.service.RatingService
import com.example.rating_service.service.SequenceGeneratorService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


@ExtendWith(MockitoExtension::class)
class RatingServiceTest {

    @Mock
    private lateinit var ratingRepo: RatingRepo

    @Mock
    private lateinit var userRatingRepo: UserRatingRepo

    @Mock
    private lateinit var sequence: SequenceGeneratorService

    @Mock
    private lateinit var ratingMapper: RatingMapper

    @Mock
    private lateinit var kafkaSenderToUser: KafkaTemplate<String, UserRatingEvent>

    @Mock
    private lateinit var kafkaSenderToRide: KafkaTemplate<String, RatingIdInRideEvent>

    @InjectMocks
    private lateinit var ratingService: RatingService

    private lateinit var ratingEntity: RatingEntity
    private lateinit var userRatingEntity: UserRatingEntity
    private lateinit var ratingRequestDto: RatingRequestDto

    @BeforeEach
    fun setUp() {
        ratingEntity = RatingEntity(
            id = 1L,
            rideId = "1a2s3d",
            rating = 5.0F,
            description = "zamechatelno",
            senderId = 1L,
            recipientId = 2L,
            senderType = SenderType.PASSENGER,
        )

        userRatingEntity = UserRatingEntity(
            id = 1L,
            recipientId = 2L,
            totalRating = 4.5F,
            quantityRating = 2
        )

        ratingRequestDto = RatingRequestDto(
            rideId = "1a2s3d",
            rating = 5.0F,
            description = "zamechatelno",
            senderId = 1L,
            recipientId = 2L,
            senderType = SenderType.PASSENGER
        )
    }

    @Test
    fun `addRating should throw MissingRequiredFieldException when senderId is invalid`() {
        val invalidRequest = ratingRequestDto.copy(senderId = 0)
        val invalidRatingEntity = ratingEntity.copy(senderId = 0)
        `when`(ratingMapper.toEntity(invalidRequest)).thenReturn(invalidRatingEntity)

        val exception = assertFailsWith<MissingRequiredFieldException> {
            ratingService.addRating(invalidRequest)
        }
        assertEquals("Sender ID and Recipient ID must be provided and greater than zero.", exception.message)
    }

    @Test
    fun `addRating should throw MissingRequiredFieldException when rating is invalid`() {
        // Arrange
        val invalidRequest = ratingRequestDto.copy(rating = 0F)
        val invalidRatingEntity = ratingEntity.copy(rating = 0F)
        `when`(ratingMapper.toEntity(invalidRequest)).thenReturn(invalidRatingEntity)

        val exception = assertFailsWith<MissingRequiredFieldException> {
            ratingService.addRating(invalidRequest)
        }
        assertEquals("Rating must be between 1 and 5.", exception.message)
    }

    @Test
    fun `addRating should throw MissingRequiredFieldException when description is missing for low rating`() {
        val invalidRequest = ratingRequestDto.copy(rating = 2F, description = null)
        val invalidRatingEntity = ratingEntity.copy(rating = 2F, description = null)
        `when`(ratingMapper.toEntity(invalidRequest)).thenReturn(invalidRatingEntity)

        val exception = assertFailsWith<MissingRequiredFieldException> {
            ratingService.addRating(invalidRequest)
        }
        assertEquals("A description is required for ratings lower than 3.", exception.message)
    }

    @Test
    fun `getRatingsForUser should return list of ratings`() {
        `when`(ratingRepo.findByRecipientId(2L)).thenReturn(listOf(ratingEntity))
        `when`(ratingMapper.toDto(ratingEntity)).thenReturn(
            RatingResponseDto(
                id = 1L,
                rideId = "1a2s3d",
                rating = 5.0F,
                description = "zamechatelno",
                senderId = 1L,
                recipientId = 2L,
                senderType = SenderType.PASSENGER,
                createdAt = ratingEntity.createdAt
            )
        )

        val result = ratingService.getRatingsForUser(2L)

        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }

    @Test
    fun `getRatingsForUser should throw NotFoundException when no ratings found`() {
        `when`(ratingRepo.findByRecipientId(2L)).thenReturn(emptyList())

        val exception = assertFailsWith<NotFoundException> {
            ratingService.getRatingsForUser(2L)
        }
        assertEquals("No ratings found for recipient with ID: 2", exception.message)
    }

    @Test
    fun `getUserRatingSummary should return user rating summary`() {
        `when`(userRatingRepo.findByRecipientId(2L)).thenReturn(userRatingEntity)
        `when`(ratingMapper.toRatingSummaryDto(userRatingEntity)).thenReturn(
            UserRatingSummaryDto(
                recipientId = 2L,
                totalRating = 4.5F,
                quantityRating = 2
            )
        )

        val result = ratingService.getUserRatingSummary(2L)

        assertEquals(2L, result.recipientId)
        assertEquals(4.5F, result.totalRating)
    }

    @Test
    fun `getUserRatingSummary should throw NotFoundException when user rating not found`() {
        `when`(userRatingRepo.findByRecipientId(2L)).thenReturn(null)

        val exception = assertFailsWith<NotFoundException> {
            ratingService.getUserRatingSummary(2L)
        }
        assertEquals("User rating summary not found for recipient with ID: 2", exception.message)
    }

    @Test
    fun `getRatingEntityById should return rating by id`() {
        `when`(ratingRepo.findById(1L)).thenReturn(Optional.of(ratingEntity))
        `when`(ratingMapper.toDto(ratingEntity)).thenReturn(
            RatingResponseDto(
                id = 1L,
                rideId = "1a2s3d",
                rating = 5.0F,
                description = "zamechatelno",
                senderId = 1L,
                recipientId = 2L,
                senderType = SenderType.PASSENGER,
                createdAt = ratingEntity.createdAt
            )
        )

        val result = ratingService.getRatingEntityById(1L)

        assertEquals(1L, result.id)
        assertEquals("1a2s3d", result.rideId)
    }

    @Test
    fun `getRatingEntityById should throw NotFoundException when rating not found`() {
        `when`(ratingRepo.findById(1L)).thenReturn(Optional.empty())

        val exception = assertFailsWith<NotFoundException> {
            ratingService.getRatingEntityById(1L)
        }
        assertEquals("Рейтинг с ID 1 не найден", exception.message)
    }
}