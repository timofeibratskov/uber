package com.example.rating_service.controller

import com.example.rating_service.dto.RatingRequestDto
import com.example.rating_service.dto.RatingResponseDto
import com.example.rating_service.dto.UserRatingSummaryDto
import com.example.rating_service.service.RatingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping

@RestController
@RequestMapping("/ratings")
@Tag(name = "Rating Management", description = "APIs for managing ratings in the ride-sharing application")
class RatingController(private val ratingService: RatingService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Add a new rating",
        description = "Create a new rating with the provided details",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Rating created successfully",
                content = [Content(schema = Schema(implementation = String::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input provided"
            )
        ]
    )
    fun addRating(@RequestBody ratingRequestDto: RatingRequestDto): ResponseEntity<String> {
        return ResponseEntity.status(201).body(ratingService.addRating(ratingRequestDto))
    }

    @GetMapping("/user/{recipientId}")
    @Operation(
        summary = "Get ratings for a user",
        description = "Retrieve all ratings associated with a specific user",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Ratings retrieved successfully",
                content = [Content(schema = Schema(implementation = RatingResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found"
            )
        ]
    )
    fun getRatingsForUser(
        @Parameter(description = "ID of the recipient user", required = true, example = "1")
        @PathVariable recipientId: Long
    ): List<RatingResponseDto> {
        return ratingService.getRatingsForUser(recipientId)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get rating by ID",
        description = "Retrieve a rating by its unique ID",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Rating retrieved successfully",
                content = [Content(schema = Schema(implementation = RatingResponseDto::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Rating not found"
            )
        ]
    )
    fun getRatingById(
        @Parameter(description = "ID of the rating", required = true, example = "1")
        @PathVariable id: Long
    ): RatingResponseDto {
        return ratingService.getRatingEntityById(id)
    }

    @GetMapping("/user/{recipientId}/summary")
    @Operation(
        summary = "Get user rating summary",
        description = "Retrieve a summary of ratings for a specific user",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Rating summary retrieved successfully",
                content = [Content(schema = Schema(implementation = UserRatingSummaryDto::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found"
            )
        ]
    )
    fun getUserRatingSummary(
        @Parameter(description = "ID of the recipient user", required = true, example = "1")
        @PathVariable recipientId: Long
    ): UserRatingSummaryDto {
        return ratingService.getUserRatingSummary(recipientId)
    }
}