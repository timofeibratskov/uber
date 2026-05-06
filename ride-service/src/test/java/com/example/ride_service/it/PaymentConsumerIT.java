package com.example.ride_service.it;

import com.example.ride_service.it.support.KafkaTestSupport;
import com.example.ride_service.model.entity.RideEntity;
import com.example.ride_service.model.enums.EventType;
import com.example.ride_service.model.enums.RideStatus;
import com.example.ride_service.model.enums.TopicType;
import com.example.ride_service.repo.db.RideRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentConsumerIT extends BaseIT {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RideRepo rideRepo;

    private KafkaTestSupport kafkaTestSupport;

    @BeforeEach
    void setUp() {
        rideRepo.deleteAll();

        kafkaTestSupport = new KafkaTestSupport(kafka.getBootstrapServers());
    }

    @Test
    @DisplayName("Должен принять событие PAYMENT_COMPLETED, пометить, что поездка оплачена и сохранить +доп инфо")
    void shouldProcessPaymentCompletedEventAndUpdateRide() throws Exception {
        // arrange
        RideEntity ride = RideEntity.builder()
                .passengerId(UUID.randomUUID())
                .status(RideStatus.COMPLETED)
                .driverId(UUID.randomUUID())
                .driverName("ivan")
                .carId(UUID.randomUUID())
                .carBrand("honda")
                .carModel("civic")
                .carColor("white")
                .carLicensePlate("1122AA-4")
                .seats(4).startAt(LocalDateTime.of(2024, 1, 1, 1, 11))
                .endAt(LocalDateTime.of(2024, 1, 1, 1, 33))
                .seats(2)
                .finalAmount(new BigDecimal("25.50"))
                .polyline("encoded_polyline_string")
                .startAddress("ул. Ленина, 1")
                .startPoint(new org.springframework.data.geo.Point(53.9, 27.5))
                .stopAddress("ул. Пушкина, 10")
                .stopPoint(new org.springframework.data.geo.Point(53.8, 27.6))
                .build();

        var savedRide = rideRepo.save(ride);

        // act
        kafkaTestSupport.sendWithHeader(
                TopicType.PAYMENT_TOPIC,
                UUID.randomUUID().toString(),
                savedRide.getId().toString(),
                "eventType",
                EventType.PAYMENT_COMPLETED.getEventName()
        );


        // assert
        await().pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    RideEntity updatedRide = rideRepo.findById(ride.getId()).orElseThrow();

                    assertTrue(updatedRide.isPaid());
                });
    }
}