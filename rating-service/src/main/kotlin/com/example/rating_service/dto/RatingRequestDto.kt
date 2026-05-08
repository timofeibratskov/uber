package com.example.rating_service.dto

import com.example.rating_service.enums.UserType
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.util.UUID


data class RatingRequestDto(
    @field:NotNull(message = "field ride id is required")
    val rideId: UUID,

    @field:NotNull(message = "field rating is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "rating must be > 0")
    @field:DecimalMax(value = "5.0", message = "rating must be ≤ 5")
    val rating: Int,

    @field:NotNull(message = "field target user id is required")
    val targetUserId: UUID,

    @field:NotNull(message = "field target user type is required")
    val targetUserType: UserType
)