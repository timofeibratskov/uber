package com.example.passenger_service.dto;

public record PassengerRatingEvent(
        Long recipientId,
        Float rating
) {
}
