package com.example.rating_service.controller;

import com.example.rating_service.model.dto.RatingRequestDto;
import com.example.rating_service.model.dto.UserRatingResponseDto;
import com.example.rating_service.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ratings")
public class RatingController {
    private final RatingService service;

    @PostMapping()
    public ResponseEntity<String> rateUser(
            @RequestBody @Valid RatingRequestDto request) {
        return ResponseEntity.status(201).body(service.rateUser(request));
    }

    @GetMapping("/users/{targetUserId}")
    public ResponseEntity<UserRatingResponseDto> getUserRating(
            @PathVariable UUID targetUserId
    ) {
        return ResponseEntity.ok(service.getUserRating(targetUserId));
    }
}
