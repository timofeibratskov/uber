package com.example.driver_service.client

import com.example.driver_service.model.dto.DriverRatingResponse
import java.util.UUID
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(value = "rating-service")
interface RatingServiceClient {
    @GetMapping("/api/v1/ratings/{targetUserId}")
    fun getUserRating(@PathVariable targetUserId: UUID): ResponseEntity<DriverRatingResponse>
}