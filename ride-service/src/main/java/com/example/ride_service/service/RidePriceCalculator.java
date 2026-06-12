package com.example.ride_service.service;

import com.example.ride_service.client.OpenRouteServiceClient;
import com.example.ride_service.exception.ExternalServiceException;
import com.example.ride_service.mapper.RideMapper;
import com.example.ride_service.model.dto.OpenRouteRequestDto;
import com.example.ride_service.model.dto.RideEstimateRequestDto;
import com.example.ride_service.model.dto.RideEstimateResponseDto;
import com.example.ride_service.repo.redis.RideEstimateCacheRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RidePriceCalculator {
    @Value("${ors.api.key}")
    private String apiKey;
    private final OpenRouteServiceClient openRouteServiceClient;
    private final RideEstimateCacheRepo rideEstimateCacheRepo;
    private final RideMapper mapper;

    public RideEstimateResponseDto calculatePrice(RideEstimateRequestDto request) {
        var coordinates = OpenRouteRequestDto.builder()
                .coordinates(
                        List.of(
                                List.of(request.startPoint().getX(), request.startPoint().getY()),
                                List.of(request.stopPoint().getX(), request.stopPoint().getY())
                        )
                ).build();

        try {
            var response = openRouteServiceClient.getGeoPath(apiKey, coordinates);


            log.info("Получен ответ от ORS для маршрута");

            double distanceMeters = response.firstRoute().summary().distance();
            double durationSeconds = response.firstRoute().summary().duration();
            String geometry = response.firstRoute().geometry();
            double distanceKm = distanceMeters / 1000.0;
            long durationMin = Math.round(durationSeconds / 60.0);

            BigDecimal price = BigDecimal.valueOf(3.0)
                    .add(BigDecimal.valueOf(distanceKm)
                            .multiply(BigDecimal.valueOf(0.8)))
                    .setScale(2, RoundingMode.HALF_UP);

            var estimateDto = RideEstimateResponseDto.builder()
                    .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                    .durationMin(durationMin)
                    .price(price)
                    .polyline(geometry)
                    .build();

            rideEstimateCacheRepo.save(mapper.toCache(estimateDto, request));
            log.info("passenger with id {} saved cache successfully", request.passengerId());

            return estimateDto;

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ExternalServiceException("price calculation failed, please try again later");
        }
    }
}
