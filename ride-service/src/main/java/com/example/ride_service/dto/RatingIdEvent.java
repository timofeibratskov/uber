package com.example.ride_service.dto;

import com.example.ride_service.enums.SenderType;

public record RatingIdEvent(
        String rideId,
        Long recipientRatingId,
        SenderType type
) {}
