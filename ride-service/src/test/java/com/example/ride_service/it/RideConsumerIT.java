package com.example.ride_service.it;

import com.example.ride_service.it.support.KafkaTestSupport;
import com.example.ride_service.model.entity.RideEntity;
import com.example.ride_service.model.enums.EventType;
import com.example.ride_service.model.enums.RideStatus;
import com.example.ride_service.model.enums.TopicType;
import com.example.ride_service.model.event.DriverAssignedEvent;
import com.example.ride_service.model.event.NoDriversEvent;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

        var event = DriverAssignedEvent.builder()
                .driverId(driverId)
                .driverName("ivan")
                .carId(UUID.randomUUID())
                .carBrand("honda")
                .carModel("civic")
                .carColor("white")
                .carLicensePlate("1122AA-4")
                .seats(4)
                .rideId(savedRide.getId())
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

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
                    assertThat(updatedRide.getDriverName()).isEqualTo(event.driverName());
                    assertThat(updatedRide.getCarLicensePlate()).isEqualTo(event.carLicensePlate());
                    assertThat(updatedRide.getCarColor()).isEqualTo(event.carColor());
                    assertThat(updatedRide.getCarBrand()).isEqualTo(event.carBrand());
                    assertThat(updatedRide.getPassengerId()).isEqualTo(passengerId);
                    assertThat(updatedRide.getFinalAmount()).isEqualByComparingTo("25.50");
                    assertThat(updatedRide.getSeats()).isEqualTo(2);
                    assertThat(updatedRide.getUpdatedAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("Должен принять событие NO_AVAILABLE_DRIVERS,сохранить как отмененную поездку")
    void shouldProcessNoDriversEventAndUpdateRide() throws Exception {
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

        String eventJson = objectMapper.writeValueAsString(
                NoDriversEvent.builder()
                        .rideId(savedRide.getId())
                        .reason("available drivers not found")
                        .build()
        );

        // act
        kafkaTestSupport.sendWithHeader(
                TopicType.RIDE_LIFECYCLE_TOPIC,
                rideId.toString(),
                eventJson,
                "eventType",
                EventType.NO_AVAILABLE_DRIVERS.getEventName()
        );

        // assert
        await().pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    RideEntity updatedRide = rideRepo.findById(rideId).orElseThrow();

                    assertThat(updatedRide.getStatus()).isEqualTo(RideStatus.CANCELLED);
                    assertNull(updatedRide.getDriverId());
                    assertNotNull(updatedRide.getCancelReasonComment());
                    assertThat(updatedRide.getPassengerId()).isEqualTo(passengerId);
                    assertThat(updatedRide.getFinalAmount()).isEqualByComparingTo("25.50");
                    assertThat(updatedRide.getSeats()).isEqualTo(2);
                    assertThat(updatedRide.getUpdatedAt()).isNotNull();
                    assertThat(updatedRide.getCancelAt()).isNotNull();
                });
    }
}