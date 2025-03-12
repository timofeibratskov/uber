package com.example.rating_service.controller

import com.example.rating_service.dto.RatingRequestDto
import com.example.rating_service.dto.RatingResponseDto
import com.example.rating_service.dto.UserRatingSummaryDto
import com.example.rating_service.service.RatingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.GetMapping

@RestController
@RequestMapping("/ratings")
class RatingController(private val ratingService: RatingService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addRating(@RequestBody ratingRequestDto: RatingRequestDto): ResponseEntity<String> {
        return ResponseEntity.status(201).body(ratingService.addRating(ratingRequestDto))
    }

    @GetMapping("/user/{recipientId}")
    fun getRatingsForUser(@PathVariable recipientId: Long): List<RatingResponseDto> {
        return ratingService.getRatingsForUser(recipientId)
    }

    @GetMapping("/{id}")
    fun getRatingById(@PathVariable id: Long): RatingResponseDto {
        return ratingService.getRatingEntityById(id)
    }

    @GetMapping("/user/{recipientId}/summary")
    fun getUserRatingSummary(@PathVariable recipientId: Long): UserRatingSummaryDto {
        return ratingService.getUserRatingSummary(recipientId)
    }
}
