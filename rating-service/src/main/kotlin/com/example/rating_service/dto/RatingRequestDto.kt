package com.example.rating_service.dto

import com.example.rating_service.enums.SenderType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class RatingRequestDto(
    @Schema(description = "ID of the ride", example = "65a1f2b3c4d5e6f7g8h9i0j")
    @NotBlank(message = "Ride ID is required")
    val rideId: String,

    @Schema(description = "Rating value (1.0 to 5.0)", example = "4.5")
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1.0")
    @Max(value = 5, message = "Rating must be at most 5.0")
    val rating: Float,

    @Schema(description = "Optional description of the rating", example = "Great ride!")
    val description: String?,

    @Schema(description = "ID of the sender", example = "1")
    @NotNull(message = "Sender ID is required")
    val senderId: Long,

    @Schema(description = "ID of the recipient", example = "2")
    @NotNull(message = "Recipient ID is required")
    val recipientId: Long,

    @Schema(description = "Type of the sender (DRIVER or PASSENGER)", example = "DRIVER")
    @NotNull(message = "Sender type is required")
    val senderType: SenderType
)