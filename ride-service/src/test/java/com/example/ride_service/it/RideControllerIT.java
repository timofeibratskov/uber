package com.example.ride_service.it;

import com.example.ride_service.model.cache.RideEstimateCache;
import com.example.ride_service.model.dto.RideCreateRequestDto;
import com.example.ride_service.model.dto.RideCreateResponseDto;
import com.example.ride_service.model.dto.RideEstimateRequestDto;
import com.example.ride_service.repo.db.RideRepo;
import com.example.ride_service.repo.redis.RideEstimateCacheRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RideControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RideEstimateCacheRepo estimateCacheRepo;

    @Autowired
    private RideRepo rideRepo;

    @BeforeEach
    void setUp() {
        estimateCacheRepo.deleteAll();
        rideRepo.deleteAll();
    }

    @Test
    @DisplayName("Должен рассчитать маршрут, вернуть 200 и сохранить данные в Redis")
    void shouldCalculateRideAndSaveToCache() throws Exception {
        // arrange
        UUID passengerId = UUID.randomUUID();
        Point startPoint = new Point(23.827427, 53.675434);
        Point stopPoint = new Point(23.782834, 53.648446);

        RideEstimateRequestDto request = new RideEstimateRequestDto(
                passengerId,
                startPoint,
                stopPoint,
                "Мостовая улица, 35",
                "улица Суворова, 302"
        );

        // act
        mockMvc.perform(post("/api/v1/rides/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // assert
        var cachedData = estimateCacheRepo.findById(passengerId);
        assertThat(cachedData).isPresent();
        assertThat(cachedData.get().getPrice()).isNotNull();
        assertThat(cachedData.get().getPolyline()).isNotNull();
    }

    @Test
    @DisplayName("Должен создать поездку в БД, используя данные из Redis кэша")
    void shouldCreateRideUsingCacheData() throws Exception {
        // arrange
        UUID passengerId = UUID.randomUUID();
        RideEstimateCache cache = RideEstimateCache.builder()
                .passengerId(passengerId)
                .price(new BigDecimal("15.00"))
                .distanceKm(5.5)
                .polyline("some_encoded_geometry")
                .startAddress("Мостовая улица, 35")
                .stopAddress("улица Суворова, 302")
                .startPoint(new Point(53.675434, 23.827427))
                .stopPoint(new Point(53.648446, 23.782834))
                .expiration(600L)
                .build();

        estimateCacheRepo.save(cache);

        RideCreateRequestDto request = new RideCreateRequestDto(passengerId, 4);

        // act
        String responseJson = mockMvc.perform(post("/api/v1/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusDescription").value("Finding driver"))
                .andReturn().getResponse().getContentAsString();

        // assert
        RideCreateResponseDto responseDto = objectMapper.readValue(responseJson, RideCreateResponseDto.class);
        var savedEntity = rideRepo.findById(responseDto.id());
        assertThat(savedEntity).isPresent();
        assertThat(savedEntity.get().getPassengerId()).isEqualTo(passengerId);
        assertThat(savedEntity.get().getFinalAmount()).isEqualByComparingTo("15.00");
        assertThat(savedEntity.get().getSeats()).isEqualTo(4);
        assertThat(estimateCacheRepo.findById(passengerId)).isEmpty();
    }
}