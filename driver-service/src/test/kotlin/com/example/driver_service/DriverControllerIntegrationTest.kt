package com.example.driver_service

import com.example.driver_service.config.TestPostgresContainer
import com.example.driver_service.dto.LoginDriverDto
import com.example.driver_service.dto.RegistrationDriverDto
import com.example.driver_service.entity.DriverEntity
import com.example.driver_service.mybatisMapper.DriverMapper
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
class DriverControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var driverMapper: DriverMapper

    @BeforeEach
    fun setUp() {
        // Очистка таблицы перед каждым тестом
        driverMapper.deleteAll()
    }

    @Test
    fun `registerDriver should return 200 and create a new driver`() {
        val registrationDto = RegistrationDriverDto(
            name = "John Doe",
            gmail = "john.doe@example.com",
            password = "password123",
            phoneNumber = "+1234567890"
        )

        mockMvc.perform(
            post("/api/drivers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.gmail").value("john.doe@example.com"))
    }

    @Test
    fun `loginDriver should return 200 and driver details`() {
        // Создаем тестового водителя напрямую через маппер
        val driver = DriverEntity(
            id = 0L, // id будет сгенерирован
            name = "John Doe",
            gmail = "john.doe@example.com",
            password = "password123",
            phoneNumber = "+1234567890",
            rating = 4.5f
        )
        driverMapper.create(driver)
        val loginDto = LoginDriverDto(
            gmail = "john.doe@example.com",
            password = "password123"
        )

        mockMvc.perform(
            post("/api/drivers/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.gmail").value("john.doe@example.com"))
    }

    @Test
    fun `getDriverById should return 200 and driver details`() {
        // Создаем тестового водителя
        val driver = DriverEntity(
            id = 0L,
            name = "John Doe",
            gmail = "john.doe@example.com",
            password = "password123",
            phoneNumber = "+1234567890",
            rating = 4.5f
        )
        driverMapper.create(driver)

        val createdDriver = driverMapper.findByEmail("john.doe@example.com")!!
        mockMvc.perform(get("/api/drivers/${createdDriver.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.gmail").value("john.doe@example.com"))
    }

    @Test
    fun `updateDriver should return 200 and updated driver details`() {
        // Создаем тестового водителя
        val driver = DriverEntity(
            id = 0L,
            name = "John Doe",
            gmail = "john.doe@example.com",
            password = "password123",
            phoneNumber = "+1234567890",
            rating = 4.5f
        )
        driverMapper.create(driver)
        val createdDriver = driverMapper.findByEmail("john.doe@example.com")!!

        // Обновляем данные
        val updatedDriverDto = mapOf(
            "id" to createdDriver.id,
            "name" to "John Updated",
            "gmail" to "john.doe@example.com",
            "password" to "password123",
            "phoneNumber" to "+1234567890",
            "rating" to 4.5
        )

        mockMvc.perform(
            put("/api/drivers/update/${createdDriver.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDriverDto))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("John Updated"))
    }

    @Test
    fun `deleteDriver should return ok`() {
        // Создаем тестового водителя
        val driver = DriverEntity(
            id = 0L,
            name = "John Doe",
            gmail = "john.doe@example.com",
            password = "password123",
            phoneNumber = "+1234567890",
            rating = 4.5f
        )
        driverMapper.create(driver)

        val loginDto = LoginDriverDto(
            gmail = "john.doe@example.com",
            password = "password123"
        )

        mockMvc.perform(
            delete("/api/drivers/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
        )
            .andExpect(status().isOk)
    }
}
