package com.example.driver_service.unit

import com.example.driver_service.model.enums.Gender
import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.model.view.CarView
import com.example.driver_service.model.view.DriverView
import com.example.driver_service.service.DriverService
import com.example.driver_service.service.LocationService
import com.example.driver_service.service.SimpleDriverMatchingService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.geo.Point

@ExtendWith(MockKExtension::class)
class SimpleDriverMatchingServiceTest {
    @MockK
    lateinit var locationService: LocationService

    @MockK
    lateinit var driverService: DriverService

    @InjectMockKs
    lateinit var driverMatchingService: SimpleDriverMatchingService

    @Test
    @DisplayName("findBestDriver: Успешное назначение водителя")
    fun findBestDriver_Success() {
        // Arrange
        val point = Point(53.0, 23.0)
        val seats = 4
        val driverId = UUID.randomUUID()
        val mockDto  = DriverView(
            driverId,
            "driver",
            "driver@gmail.com",
            "+375295865864",
            BigDecimal.valueOf(5.0),
            Gender.MALE,
            CarView(
                UUID.randomUUID(),
                "red",
                "3303AM-2",
                "fiat",
                "1",
                5
            )
        )

        every { locationService.getAvailableIds(point) } returns listOf(driverId)
        every { driverService.findAllAvailableDrivers(listOf(driverId), seats) } returns listOf(mockDto)
        every { driverService.setWorkStatus(driverId, WorkStatus.BUSY) } returns Unit

        // Act
        val result = driverMatchingService.findBestDriver(point, seats)

        // Assert
        assertEquals(mockDto, result)
        verify(exactly = 1) { driverService.setWorkStatus(driverId, WorkStatus.BUSY) }
    }

    @Test
    @DisplayName("findBestDriver: Исключение, если подходящих водителей не найдено")
    fun findBestDriver_returnNull_WhenNoDrivers() {
        // Arrange
        val point = Point(53.0, 23.0)
        val seats = 4

        every { locationService.getAvailableIds(point) } returns listOf(UUID.randomUUID())
        every { driverService.findAllAvailableDrivers(any(), any()) } returns emptyList()

        // Act
        val result = driverMatchingService.findBestDriver(point, seats)

        // Assert
        assertNull(result)
        verify(exactly = 0) { driverService.setWorkStatus(any(), any()) }
    }
}