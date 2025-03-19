package com.example.driver_service

import com.example.driver_service.client.RideServiceClient
import com.example.driver_service.dto.DriverNotification
import com.example.driver_service.dto.DriverRatingEvent
import com.example.driver_service.entity.DriverEntity
import com.example.driver_service.exception.DriverNotFoundException
import com.example.driver_service.exception.EmailNotFoundException
import com.example.driver_service.exception.EmailAlreadyExistsException
import com.example.driver_service.exception.PhoneNumberAlreadyExistsException
import com.example.driver_service.exception.InvalidCredentialsException
import com.example.driver_service.exception.NameAlreadyExistsException
import com.example.driver_service.mybatisMapper.CarMapper
import com.example.driver_service.mybatisMapper.DriverMapper
import com.example.driver_service.service.DriverService
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockitoExtension::class)
class DriverServiceTest {

    @Mock
    private lateinit var driverMapper: DriverMapper

    @Mock
    private lateinit var carMapper: CarMapper

    @Mock
    private lateinit var rideClient: RideServiceClient

    @Mock
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @InjectMocks
    private lateinit var driverService: DriverService

    private lateinit var driver: DriverEntity

    @BeforeEach
    fun setUp() {
        driver = DriverEntity(
            id = 1L,
            gmail = "driver@example.com",
            password = "password",
            phoneNumber = "1234567890",
            name = "Driver Name",
            rating = 4.5F
        )
    }

    @Test
    fun `getAllDrivers should return list of drivers`() {
        `when`(driverMapper.findAll()).thenReturn(listOf(driver))

        val drivers = driverService.getAll()

        assertNotNull(drivers)
        assertEquals(1, drivers.size)
        assertEquals(driver, drivers[0])
    }

    @Test
    fun `getDriverById should return driver when found`() {
        `when`(driverMapper.findById(1L)).thenReturn(driver)

        val foundDriver = driverService.getDriverById(1L)

        assertNotNull(foundDriver)
        assertEquals(driver, foundDriver)
    }

    @Test
    fun `getDriverById should throw DriverNotFoundException when not found`() {
        `when`(driverMapper.findById(1L)).thenReturn(null)

        assertThrows(DriverNotFoundException::class.java) {
            driverService.getDriverById(1L)
        }
    }

    @Test
    fun `login should return driver when credentials are valid`() {
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(driver)

        val loggedInDriver = driverService.login("driver@example.com", "password")

        assertNotNull(loggedInDriver)
        assertEquals(driver, loggedInDriver)
    }

    @Test
    fun `login should throw EmailNotFoundException when email not found`() {
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(null)

        assertThrows(EmailNotFoundException::class.java) {
            driverService.login("driver@example.com", "password")
        }
    }

    @Test
    fun `login should throw InvalidCredentialsException when password is incorrect`() {
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(driver)

        assertThrows(InvalidCredentialsException::class.java) {
            driverService.login("driver@example.com", "wrongpassword")
        }
    }

    @Test
    fun `createDriver should return created driver when successful`() {
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(null)
        `when`(driverMapper.findByPhoneNumber("1234567890")).thenReturn(null)
        `when`(driverMapper.findByName("Driver Name")).thenReturn(null)
        `when`(driverMapper.create(driver)).thenReturn(1)
        `when`(driverMapper.findById(1L)).thenReturn(driver)

        val createdDriver = driverService.createDriver(driver)

        assertNotNull(createdDriver)
        assertEquals(driver, createdDriver)
    }

    @Test
    fun `createDriver should throw EmailAlreadyExistsException when email exists`() {
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(driver)

        assertThrows(EmailAlreadyExistsException::class.java) {
            driverService.createDriver(driver)
        }
    }

    @Test
    fun `createDriver should throw PhoneNumberAlreadyExistsException when phone number exists`() {
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(null)
        `when`(driverMapper.findByPhoneNumber("1234567890")).thenReturn(driver)

        assertThrows(PhoneNumberAlreadyExistsException::class.java) {
            driverService.createDriver(driver)
        }
    }

    @Test
    fun `createDriver should throw NameAlreadyExistsException when name exists`() {
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(null)
        `when`(driverMapper.findByPhoneNumber("1234567890")).thenReturn(null)
        `when`(driverMapper.findByName("Driver Name")).thenReturn(driver)

        assertThrows(NameAlreadyExistsException::class.java) {
            driverService.createDriver(driver)
        }
    }

    @Test
    fun `updateDriver should return updated driver when successful`() {
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(null)
        `when`(driverMapper.findByPhoneNumber("1234567890")).thenReturn(null)
        `when`(driverMapper.findByName("Driver Name")).thenReturn(null)
        `when`(driverMapper.update(driver)).thenReturn(1)

        val updatedDriver = driverService.updateDriver(driver)

        assertNotNull(updatedDriver)
        assertEquals(driver, updatedDriver)
    }

    @Test
    fun `updateDriver should throw EmailAlreadyExistsException when email exists`() {
        val existingDriver = DriverEntity(
            id = 2L,
            gmail = "driver@example.com",
            name = "superman228",
            password = "1111",
            phoneNumber = "+375464665774",
            rating = 5.0F
        )
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(existingDriver)

        assertThrows(EmailAlreadyExistsException::class.java) {
            driverService.updateDriver(driver)
        }
    }

    @Test
    fun `updateDriver should throw PhoneNumberAlreadyExistsException when phone number exists`() {
        val existingDriver = DriverEntity(
            id = 2L,
            gmail = "driver@example.com",
            name = "superman228",
            password = "1111",
            phoneNumber = "+375464665774",
            rating = 5.0F
        )
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(null)
        `when`(driverMapper.findByPhoneNumber("1234567890")).thenReturn(existingDriver)

        assertThrows(PhoneNumberAlreadyExistsException::class.java) {
            driverService.updateDriver(driver)
        }
    }

    @Test
    fun `updateDriver should throw NameAlreadyExistsException when name exists`() {
        val existingDriver = DriverEntity(
            id = 2L,
            gmail = "driver@example.com",
            name = "superman228",
            password = "1111",
            phoneNumber = "+375464665774",
            rating = 5.0F
        )
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(null)
        `when`(driverMapper.findByPhoneNumber("1234567890")).thenReturn(null)
        `when`(driverMapper.findByName("Driver Name")).thenReturn(existingDriver)

        assertThrows(NameAlreadyExistsException::class.java) {
            driverService.updateDriver(driver)
        }
    }

    @Test
    fun `deleteDriver should not throw exception when successful`() {
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(driver)
        `when`(driverMapper.delete(1L)).thenReturn(1)

        assertDoesNotThrow {
            driverService.deleteDriver("driver@example.com", "password")
        }
    }

    @Test
    fun `deleteDriver should throw EmailNotFoundException when email not found`() {
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(null)

        assertThrows(EmailNotFoundException::class.java) {
            driverService.deleteDriver("driver@example.com", "password")
        }
    }

    @Test
    fun `deleteDriver should throw InvalidCredentialsException when password is incorrect`() {
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(driver)

        assertThrows(InvalidCredentialsException::class.java) {
            driverService.deleteDriver("driver@example.com", "wrongpassword")
        }
    }

    @Test
    fun `deleteDriver should throw DriverNotFoundException when driver not found`() {
        `when`(driverMapper.findByEmail("driver@example.com")).thenReturn(driver)
        `when`(driverMapper.delete(1L)).thenReturn(0)

        assertThrows(DriverNotFoundException::class.java) {
            driverService.deleteDriver("driver@example.com", "password")
        }
    }

    @Test
    fun `sendNotificationsForDriver should assign driver when drivers are available`() {
        val notification =
            DriverNotification(id = "123ae1232aszz", seats = 4, pointA = "AAA", pointB = "BBB", creatorId = 1L)
        `when`(carMapper.findDriversIdBySeats(4)).thenReturn(listOf(1L))

        driverService.sendNotificationsForDriver(notification)

        verify(rideClient, times(1)).assignDriver("123ae1232aszz", 1L)
    }

    @Test
    fun `sendNotificationsForDriver should send message to Kafka when no drivers are available`() {
        val notification = DriverNotification(id = "123asd", seats = 4, pointA = "aaa", pointB = "bbb", creatorId = 1)
        `when`(carMapper.findDriversIdBySeats(4)).thenReturn(emptyList())

        driverService.sendNotificationsForDriver(notification)

        verify(kafkaTemplate, times(1)).send("drivers-not-found", notification.id)
    }

    @Test
    fun `setRatingForDriver should update driver rating when driver is found`() {
        val event = DriverRatingEvent(recipientId = 1L, rating = 5.0F)
        `when`(driverMapper.findById(1L)).thenReturn(driver)

        driverService.setRatingForDriver(event)

        assertEquals(5.0F, driver.rating)
        verify(driverMapper, times(1)).update(driver)
    }

    @Test
    fun `setRatingForDriver should throw DriverNotFoundException when driver is not found`() {
        val event = DriverRatingEvent(recipientId = 1L, rating = 5.0F)
        `when`(driverMapper.findById(1L)).thenReturn(null)

        assertThrows(DriverNotFoundException::class.java) {
            driverService.setRatingForDriver(event)
        }
    }
}