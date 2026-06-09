package com.example.passenger_service.client;

import com.example.passenger_service.model.dto.PassengerRatingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(value = "rating-service")
public interface RatingServiceClient {
    @GetMapping("/api/v1/ratings/{targetUserId}")
    ResponseEntity<PassengerRatingResponse> getUserRating(@PathVariable UUID targetUserId);
}

