package com.example.ride_service;


import com.example.ride_service.dto.RideRequestDto;
import com.example.ride_service.entity.RideEntity;
import com.example.ride_service.enums.RideStatus;
import com.example.ride_service.repo.RideRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = RideServiceApplication.class)
@TestPropertySource(properties = {
        "server.port=8083",
        "management.server.port=8083",
        "spring.kafka.bootstrap-servers=localhost:29092", // Подключение к вашей продакшен Kafka
        "spring.data.mongodb.uri=mongodb://localhost:27017/test" // Подключение к MongoDB
})
public class RideServiceE2ETest {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.22")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private RideRepo repo;

    @Test
    void testRideCreationFlow() {
        // 1. Подготовка тестовых данных
        RideRequestDto request = new RideRequestDto(
                "Main St", "Elm St", 12365L, (byte) 1);

        // 2. Отправка запроса
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "http://localhost:8083/api/rides",
                request,
                Void.class
        );

        // 3. Проверка ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 4. Проверка конечного состояния в БД
        await().atMost(15, SECONDS)
                .untilAsserted(() -> {
                    RideEntity ride = repo.findAllByCreatorId(12365L).getFirst();

                    assertThat(ride.getStatus())
                            .isIn(RideStatus.DRIVER_FOUND);
                });

    }

}