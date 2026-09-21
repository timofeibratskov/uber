package com.example.rating_service.controller.rest;

import com.example.rating_service.model.dto.RatingRequestDto;
import com.example.rating_service.model.dto.UserRatingResponseDto;
import com.example.rating_service.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Рейтинги и оценки", description = "Управление оценками и расчетом среднего рейтинга водителей и пассажиров")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ratings")
public class RatingController {

    private final RatingService service;

    @Operation(
            summary = "Оценить пользователя",
            description = "Позволяет оставить оценку (от 1 до 5) и отзыв по завершении поездки для водителя или пассажира"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Оценка успешно выставлена",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(implementation = String.class, example = "Rating submitted successfully")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные (например, оценка вне диапазона 1-5)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь или поездка не найдены", content = @Content)
    })
    @PostMapping()
    public ResponseEntity<String> rateUser(
            @RequestBody @Valid RatingRequestDto request) {
        return ResponseEntity.status(201).body(service.rateUser(request));
    }

    @Operation(
            summary = "Получить средний рейтинг пользователя",
            description = "Возвращает среднюю оценку пользователя и общее количество выставленных ему звезд"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Рейтинг пользователя успешно получен"),
            @ApiResponse(responseCode = "404", description = "Пользователь с таким ID не найден в системе", content = @Content)
    })
    @GetMapping("/users/{targetUserId}")
    public ResponseEntity<UserRatingResponseDto> getUserRating(
            @Parameter(description = "Уникальный идентификатор целевого пользователя (кого оценивают)", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID targetUserId
    ) {
        return ResponseEntity.ok(service.getUserRating(targetUserId));
    }
}
