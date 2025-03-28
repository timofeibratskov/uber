package com.example.ride_service;

import com.example.ride_service.dto.RideDto;
import com.example.ride_service.dto.RideRequestDto;
import com.example.ride_service.enums.RideStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class RideControllerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.22")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String createdRideId;

    @BeforeEach
    void setUp() throws Exception {
        // Создаем тестовую поездку перед каждым тестом
        RideRequestDto request = new RideRequestDto(
                "ул. Пушкина, 15",
                "ул. Пушкина, 21",
                1L,
                (byte) 4
        );

        MvcResult result = mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Получаем ID созданной поездки для использования в других тестах
        createdRideId = result.getResponse().getContentAsString();
    }

    @Test
    void shouldCreateRideSuccessfully() throws Exception {
        RideRequestDto validRequest = new RideRequestDto(
                "ул. Лермонтова, 10",
                "ул. Гоголя, 5",
                2L,
                (byte) 3
        );

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetPassengerRides() throws Exception {
        mockMvc.perform(get("/api/rides/passenger/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].creatorId").value(1))  // изменили passengerId на creatorId
                .andExpect(jsonPath("$[0].pointA").value("ул. Пушкина, 15"))
                .andExpect(jsonPath("$[0].pointB").value("ул. Пушкина, 21"));
    }


    @Test
    void shouldReturnNotFoundForNonExistingDriver() throws Exception {
        // Проверяем случай, когда у водителя нет поездок
        mockMvc.perform(get("/api/rides/driver/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Поезки для водителя с таким id не существует"));
    }

    @Test
    void shouldGetRidesByStatus() throws Exception {
        mockMvc.perform(get("/api/rides/status/CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }


    @Test
    void shouldReturnNotFoundForInvalidRideId() throws Exception {
        String invalidRideId = "invalid123";

        mockMvc.perform(get("/api/rides/ride/" + invalidRideId))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/rides/" + invalidRideId + "/assign-driver?driverId=5"))
                .andExpect(status().isNotFound());
    }
}