package com.example.rating_service.unit

import com.example.rating_service.dto.RatingRequestDto
import com.example.rating_service.entity.RatingEntity
import com.example.rating_service.entity.UserRatingEntity
import com.example.rating_service.repo.RatingRepo
import com.example.rating_service.repo.UserRatingRepo
import com.example.rating_service.service.RatingService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import java.math.BigDecimal
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
class RatingServiceTest {
    @MockK
    lateinit var ratingRepo: RatingRepo

    @MockK
    lateinit var userRatingRepo: UserRatingRepo

    @InjectMockKs
    lateinit var ratingService: RatingService

    @Test
    fun `addRating - positive - new user receives first rating`() {
        // Arrange
        val rideId = UUID.randomUUID()
        val targetUserId = UUID.randomUUID()
        val request = RatingRequestDto(
            rideId = rideId,
            targetUserId = targetUserId,
            rating = BigDecimal.valueOf(4.5)
        )

        every { ratingRepo.existsByRideId(rideId) } returns false
        every { userRatingRepo.findByUserId(targetUserId) } returns null
        every { ratingRepo.save(any<RatingEntity>()) } returnsArgument 0
        every { userRatingRepo.save(any<UserRatingEntity>()) } returnsArgument 0

        // Act
        val response = ratingService.addRating(request)

        // Assert
        assertEquals("Rating received and will be processed", response.message)
        assertEquals(targetUserId, response.targetUserId)
        verify(exactly = 1) { ratingRepo.save(any<RatingEntity>()) }
        verify(exactly = 1) { userRatingRepo.save(any<UserRatingEntity>()) }
    }

    @Test
    fun `addRating - negative - ride already rated throws exception`() {
        // Arrange
        val rideId = UUID.randomUUID()
        val targetUserId = UUID.randomUUID()
        val request = RatingRequestDto(
            rideId = rideId,
            targetUserId = targetUserId,
            rating = BigDecimal.valueOf(4.5)
        )

        every { ratingRepo.existsByRideId(rideId) } returns true

        // Act
        val exception = assertThrows<RuntimeException> {
            ratingService.addRating(request)
        }
        // Assert
        assertEquals("Ride with id $rideId already rated", exception.message)
        verify(exactly = 0) { ratingRepo.save(any<RatingEntity>()) }
        verify(exactly = 0) { userRatingRepo.save(any<UserRatingEntity>()) }
    }

    @Test
    fun `addRating - positive - existing user receives new rating updates correctly`() {
        // Arrange
        val rideId = UUID.randomUUID()
        val targetUserId = UUID.randomUUID()
        val request = RatingRequestDto(
            rideId = rideId,
            targetUserId = targetUserId,
            rating = BigDecimal.valueOf(4.0)
        )

        val existingUser = UserRatingEntity(
            id = UUID.randomUUID(),
            userId = targetUserId,
            count = 3,
            totalScore = BigDecimal.valueOf(12.5),
            rating = BigDecimal.valueOf(4.17),
        )

        every { ratingRepo.existsByRideId(rideId) } returns false
        every { userRatingRepo.findByUserId(targetUserId) } returns existingUser
        every { ratingRepo.save(any<RatingEntity>()) } returnsArgument 0
        every { userRatingRepo.save(any<UserRatingEntity>()) } returnsArgument 0

        // Act
        val response = ratingService.addRating(request)

        // Assert
        assertEquals("Rating received and will be processed", response.message)
        assertEquals(targetUserId, response.targetUserId)

        verify(exactly = 1) { userRatingRepo.save(any<UserRatingEntity>()) }
        verify(exactly = 1) { ratingRepo.save(any<RatingEntity>()) }
    }
}