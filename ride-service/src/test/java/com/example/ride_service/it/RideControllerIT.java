package com.example.ride_service.it;

import com.example.ride_service.client.OpenRouteServiceClient;
import com.example.ride_service.it.support.KafkaTestSupport;
import com.example.ride_service.model.cache.RideEstimateCache;
import com.example.ride_service.model.dto.RideCancelRequestDto;
import com.example.ride_service.model.dto.RideCreateRequestDto;
import com.example.ride_service.model.dto.RideCreateResponseDto;
import com.example.ride_service.model.dto.RideEndResponseDto;
import com.example.ride_service.model.dto.RideEstimateRequestDto;
import com.example.ride_service.model.entity.RideEntity;
import com.example.ride_service.model.enums.CancelInitiator;
import com.example.ride_service.model.enums.EventType;
import com.example.ride_service.model.enums.RideStatus;
import com.example.ride_service.model.enums.TopicType;
import com.example.ride_service.repo.db.OutboxEventRepo;
import com.example.ride_service.repo.db.RideRepo;
import com.example.ride_service.repo.redis.RideEstimateCacheRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @Autowired
    private OutboxEventRepo outboxRepo;

    @MockitoBean
    private OpenRouteServiceClient openRouteServiceClient;

    private KafkaTestSupport kafkaTestSupport;

    @BeforeEach
    void setUp() {
        estimateCacheRepo.deleteAll();
        rideRepo.deleteAll();
        outboxRepo.deleteAll();

        this.kafkaTestSupport = new KafkaTestSupport(kafka.getBootstrapServers());
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
        String jsonResponse = """
                {
                    "routes": [{
                        "summary": {
                            "distance": 5200.0,
                            "duration": 900.0
                        },
                        "geometry": "mock_encoded_polyline"
                    }]
                }
                """;
        var mockResponse = objectMapper.readTree(jsonResponse);

        when(openRouteServiceClient.fetchRoute(any(Point.class), any(Point.class)))
                .thenReturn(mockResponse);

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
        var outboxEntities = outboxRepo.findAllByOrderByCreatedAt();

        assertThat(outboxEntities).hasSize(1);

        var outboxEntity = outboxEntities.getFirst();
        assertNotNull(outboxEntity.getId());
        assertNotNull(outboxEntity.getCreatedAt());
        assertEquals(TopicType.RIDE_LIFECYCLE, outboxEntity.getTopic());
        assertEquals(EventType.RIDE_CREATED, outboxEntity.getEventType());
        assertNotNull(outboxEntity.getPayload());

        assertThat(savedEntity).isPresent();
        assertThat(savedEntity.get().getPassengerId()).isEqualTo(passengerId);
        assertThat(savedEntity.get().getFinalAmount()).isEqualByComparingTo("15.00");
        assertThat(savedEntity.get().getSeats()).isEqualTo(4);
        assertThat(estimateCacheRepo.findById(passengerId)).isEmpty();

        await().pollInterval(Duration.ofSeconds(6))
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() ->
                        assertThat(outboxRepo.findAllByOrderByCreatedAt()).isEmpty()
                );

        try (KafkaConsumer<String, String> consumer = kafkaTestSupport.createConsumer()) {
            consumer.subscribe(List.of(TopicType.RIDE_LIFECYCLE.getTopicName()));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

            ConsumerRecord<String, String> rideEvent = null;
            for (ConsumerRecord<String, String> record : records) {
                if (EventType.RIDE_CREATED.getEventName().equals(kafkaTestSupport.getEventType(record))) {
                    rideEvent = record;
                    break;
                }
            }

            assertThat(rideEvent)
                    .withFailMessage("RIDE_CREATED event not found in Kafka")
                    .isNotNull();

            var payload = objectMapper.readTree(rideEvent.value());
            assertThat(payload.get("rideId").asText()).isEqualTo(responseDto.id().toString());
            assertThat(payload.get("seats").asInt()).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("успешная отмена поездки")
    void shouldCancelRideAndSaveSuccessfully() throws Exception {
        // arrange
        RideEntity entity = RideEntity.builder()
                .seats(4)
                .polyline("randomPolyline")
                .finalAmount(new BigDecimal("15.00"))
                .startAddress("address1")
                .startPoint(new Point(53.675434, 23.827427))
                .stopAddress("address2")
                .stopPoint(new Point(53.648446, 23.782834))
                .passengerId(UUID.randomUUID())
                .status(RideStatus.CREATED)
                .build();

        rideRepo.save(entity);

        var request = RideCancelRequestDto.builder()
                .cancelInitiator(CancelInitiator.PASSENGER)
                .comment("123")
                .build();

        // act
        mockMvc.perform(patch("/api/v1/rides/" + entity.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // assert
        var savedEntity = rideRepo.findById(entity.getId());
        assertThat(savedEntity).isPresent();
        assertEquals(RideStatus.CANCELLED, savedEntity.get().getStatus());
        assertEquals(CancelInitiator.PASSENGER, savedEntity.get().getCancelInitiator());
        assertNotNull(savedEntity.get().getCancelAt());
        assertNotNull(savedEntity.get().getCancelReasonComment());
    }

    @Test
    @DisplayName("успешное начало поездки")
    void shouldStartRide_whenRideExistsAndStatusIsValid_shouldUpdateRideSuccessfully() throws Exception {
        // arrange
        RideEntity entity = RideEntity.builder()
                .seats(4)
                .polyline("randomPolyline")
                .finalAmount(new BigDecimal("15.00"))
                .startAddress("address1")
                .startPoint(new Point(53.675434, 23.827427))
                .stopAddress("address2")
                .stopPoint(new Point(53.648446, 23.782834))
                .passengerId(UUID.randomUUID())
                .driverId(UUID.randomUUID())
                .status(RideStatus.ACCEPTED)
                .build();

        rideRepo.save(entity);

        // act
        mockMvc.perform(patch("/api/v1/rides/" + entity.getId() + "/start")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        // assert
        var savedEntity = rideRepo.findById(entity.getId());
        assertThat(savedEntity).isPresent();
        assertEquals(RideStatus.STARTED, savedEntity.get().getStatus());
        assertNotNull(savedEntity.get().getStartAt());
        assertNull(savedEntity.get().getEndAt());
    }

    @Test
    @DisplayName("успешное завершение поездки")
    void shouldEndRide_whenRideExistsAndStatusIsValid_shouldUpdateRideSuccessfully() throws Exception {
        // arrange
        RideEntity entity = RideEntity.builder()
                .seats(4)
                .polyline("randomPolyline")
                .finalAmount(new BigDecimal("15.00"))
                .startAddress("address1")
                .startPoint(new Point(53.675434, 23.827427))
                .stopAddress("address2")
                .stopPoint(new Point(53.648446, 23.782834))
                .passengerId(UUID.randomUUID())
                .driverId(UUID.randomUUID())
                .startAt(LocalDateTime.of(2020, 1, 1, 0, 0, 0))
                .status(RideStatus.STARTED)
                .build();

        rideRepo.save(entity);

        // act
        var response = mockMvc.perform(patch("/api/v1/rides/" + entity.getId() + "/end")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // assert
        String content = response.getResponse().getContentAsString(StandardCharsets.UTF_8);
        var responseDto = objectMapper.readValue(content, RideEndResponseDto.class);

        var savedEntity = rideRepo.findById(entity.getId());

        assertThat(savedEntity).isPresent();
        assertEquals(RideStatus.COMPLETED, savedEntity.get().getStatus());
        assertNotNull(savedEntity.get().getStartAt());
        assertNotNull(savedEntity.get().getEndAt());
        assertNotNull(responseDto.durationMinutes());
        assertNotNull(responseDto.finalAmount());
        assertEquals(RideStatus.COMPLETED, responseDto.status());
    }
}