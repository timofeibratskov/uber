package com.example.rating_service.controller

import com.example.rating_service.dto.RatingRequestDto
import com.example.rating_service.dto.RatingResponseDto
import com.example.rating_service.service.RatingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/v1/ratings")
class RatingController(
    private val ratingService: RatingService
) {
    @PostMapping
    fun addRating(@RequestBody @Valid ratingRequestDto: RatingRequestDto):
            ResponseEntity<RatingResponseDto> {
        return ResponseEntity.ok(ratingService.addRating(ratingRequestDto))
    }
}
