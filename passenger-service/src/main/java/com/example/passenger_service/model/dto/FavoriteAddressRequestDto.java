package com.example.passenger_service.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FavoriteAddressRequestDto(
        @NotBlank(message = "Заголовок не может быть пустым!")
        String label,

        @NotBlank(message = "Адрес не может быть пустым!")
        String address,

        @NotNull(message = "Широта обязательна!")
        Double latitude,

        @NotNull(message = "Долгота обязательна!")
        Double longitude
) {
}
