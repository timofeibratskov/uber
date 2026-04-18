package com.example.ride_service.it;

import com.example.ride_service.it.support.KafkaTestSupport;
import com.example.ride_service.model.entity.RideEntity;
import com.example.ride_service.model.enums.EventType;
import com.example.ride_service.model.enums.RideStatus;
import com.example.ride_service.model.enums.TopicType;
import com.example.ride_service.model.event.DriverAssignedEvent;
import com.example.ride_service.repo.db.RideRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class RideConsumerIT extends BaseIT {

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
    @DisplayName("Должен принять событие ASSIGNED_DRIVER, обновить статус поездки и сохранить driverId+доп инфо")
    void shouldProcessDriverAssignedEventAndUpdateRide() throws Exception {
        // arrange

        UUID passengerId = UUID.randomUUID();

        RideEntity ride = RideEntity.builder()
                .passengerId(passengerId)
                .status(RideStatus.CREATED)
                .seats(2)
                .finalAmount(new BigDecimal("25.50"))
                .polyline("encoded_polyline_string")
                .startAddress("ул. Ленина, 1")
                .startPoint(new org.springframework.data.geo.Point(53.9, 27.5))
                .stopAddress("ул. Пушкина, 10")
                .stopPoint(new org.springframework.data.geo.Point(53.8, 27.6))
                .build();

        var savedRide = rideRepo.save(ride);
        UUID rideId = savedRide.getId();
        UUID driverId = UUID.randomUUID();

        String eventJson = objectMapper.writeValueAsString(
                DriverAssignedEvent.builder()
                        .driverId(driverId)
                        .driverName("ivan")
                        .carId(UUID.randomUUID())
                        .carBrand("honda")
                        .carModel("civic")
                        .carColor("white")
                        .carLicensePlate("1122AA-4")
                        .seats(4)
                        .rideId(savedRide.getId())
                        .build()
        );

        // act
        kafkaTestSupport.sendWithHeader(
                TopicType.RIDE_LIFECYCLE_TOPIC,
                rideId.toString(),
                eventJson,
                "eventType",
                EventType.ASSIGNED_DRIVER.getEventName()
        );

        // assert
        await().pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    RideEntity updatedRide = rideRepo.findById(rideId).orElseThrow();

                    assertThat(updatedRide.getStatus()).isEqualTo(RideStatus.ACCEPTED);
                    assertThat(updatedRide.getDriverId()).isEqualTo(driverId);

                    RideEntity finalRide = rideRepo.findById(rideId).orElseThrow();

                    assertThat(finalRide.getStatus()).isEqualTo(RideStatus.ACCEPTED);
                    assertThat(finalRide.getDriverId()).isEqualTo(driverId);
                    assertThat(finalRide.getPassengerId()).isEqualTo(passengerId);
                    assertThat(finalRide.getFinalAmount()).isEqualByComparingTo("25.50");
                    assertThat(finalRide.getSeats()).isEqualTo(2);
                    assertThat(finalRide.getUpdatedAt()).isNotNull();
                });
    }
}