package com.example.ride_service;


import com.example.ride_service.dto.RideRequestDto;
import com.example.ride_service.entity.RideEntity;
import com.example.ride_service.enums.RideStatus;
import com.example.ride_service.repo.RideRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = RideServiceApplication.class)
@TestPropertySource(properties = {
        "server.port=8083",
        "spring.kafka.bootstrap-servers=localhost:29092",
        "spring.data.mongodb.uri=mongodb://localhost:27017/test"
})
public class RideServiceE2ETest {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.22")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private RideRepo repo;

    @Test
    void testRideCreationFlow() {
        // 1. Подготовка тестовых данных
        RideRequestDto request = new RideRequestDto(
                "Main St", "Elm St", 123657L, (byte) 1);



        // 2. Отправка запроса
          restTemplate.postForEntity(
                "http://localhost:8083/api/rides",
                request,
                Void.class
        );

        String rideId = repo.findAllByCreatorId(request.getCreatorId()).getFirst().getId();
         restTemplate.exchange(
                "http://localhost:8083/api/rides/{rideId}/start",
                HttpMethod.PUT,
                null,
                String.class,
                rideId
        );
         restTemplate.exchange(
                "http://localhost:8083/api/rides/{rideId}/complete",
                HttpMethod.PUT,
                null,
                String.class,
                rideId
        );
        RideEntity ride = repo.findAllByCreatorId(request.getCreatorId()).getFirst();

        assertThat(ride.getStatus())
                .isIn(RideStatus.COMPLETED);
    }

}