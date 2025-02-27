package com.example.payment_service.dto;


public record UpdateCardPasswordDto(
        Integer password,
        Integer newPassword
) {
}
