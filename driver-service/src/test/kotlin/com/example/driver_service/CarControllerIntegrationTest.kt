package com.example.driver_service

import com.example.driver_service.config.TestPostgresContainer
import com.example.driver_service.dto.CarDto
import com.example.driver_service.entity.CarEntity
import com.example.driver_service.entity.DriverEntity
import com.example.driver_service.mybatisMapper.CarMapper
import com.example.driver_service.mybatisMapper.DriverMapper
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.junit.jupiter.Testcontainers

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestPostgresContainer.Initializer::class])
@Testcontainers
class CarControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var carMapper: CarMapper

    @Autowired
    private lateinit var driverMapper: DriverMapper

    @BeforeEach
    fun setUp() {
        // Очистка таблиц перед каждым тестом (предполагается, что методы deleteAll() реализованы)
        carMapper.deleteAll()
        driverMapper.deleteAll()
        // Создаем тестового водителя, чтобы иметь валидный driver_id для автомобиля
        val driver = DriverEntity(
            id = 0L, // id будет сгенерирован
            name = "Test Driver",
            gmail = "test.driver@example.com",
            password = "testPass",
            phoneNumber = "+1111111111",
            rating = 5.0f
        )
        driverMapper.create(driver)
    }

    @Test
    fun `getAllCars should return empty list when no cars exist`() {
        mockMvc.perform(get("/api/cars/all"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `createCar should return created car`() {
        // Получаем тестового водителя для передачи его id в запросе
        val driver = driverMapper.findByEmail("test.driver@example.com")!!
        val carDto = CarDto(
            id = null,
            driverId = driver.id, // значение будет перезаписано контроллером из path variable
            color = "Blue",
            licensePlate = "ABC123",
            brand = "Toyota",
            seats = 4
        )

        mockMvc.perform(
            post("/api/cars/create/${driver.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(carDto))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.color").value("Blue"))
            .andExpect(jsonPath("$.licensePlate").value("ABC123"))
    }

    @Test
    fun `getCarById should return car details`() {
        val driver = driverMapper.findByEmail("test.driver@example.com")!!
        // Создаем тестовый автомобиль
        val car = CarEntity(
            id = 0L,
            driverId = driver.id,
            color = "Red",
            licensePlate = "XYZ789",
            brand = "Honda",
            seats = 5
        )
        carMapper.create(car)
        // Получаем созданный автомобиль по уникальному license plate
        val createdCar = carMapper.findByLicensePlate("XYZ789")
        assertNotNull(createdCar)

        mockMvc.perform(get("/api/cars/${createdCar!!.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.brand").value("Honda"))
            .andExpect(jsonPath("$.color").value("Red"))
    }

    @Test
    fun `updateCar should return updated car details`() {
        val driver = driverMapper.findByEmail("test.driver@example.com")!!
        val car = CarEntity(
            id = 0L,
            driverId = driver.id,
            color = "Green",
            licensePlate = "LMN456",
            brand = "Ford",
            seats = 4
        )
        carMapper.create(car)
        val createdCar = carMapper.findByLicensePlate("LMN456")!!
        // Готовим DTO с обновленными данными
        val updatedCarDto = CarDto(
            id = createdCar.id,
            driverId = driver.id,
            color = "Yellow",
            licensePlate = "LMN456", // license plate остаётся прежним
            brand = "Ford",
            seats = 4
        )

        mockMvc.perform(
            put("/api/cars/update/${createdCar.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCarDto))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.color").value("Yellow"))
    }

    @Test
    fun `deleteCar should return ok`() {
        val driver = driverMapper.findByEmail("test.driver@example.com")!!
        val car = CarEntity(
            id = 0L,
            driverId = driver.id,
            color = "Black",
            licensePlate = "DEL123",
            brand = "Chevrolet",
            seats = 4
        )
        carMapper.create(car)
        val createdCar = carMapper.findByLicensePlate("DEL123")!!
        mockMvc.perform(delete("/api/cars/delete/${createdCar.id}"))
            .andExpect(status().isOk)
    }
}
