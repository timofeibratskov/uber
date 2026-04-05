package com.example.driver_service.unit

import com.example.driver_service.constant.RedisSchema
import com.example.driver_service.exception.DriverNotFoundException
import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.service.LocationService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.geo.Point
import org.springframework.data.redis.core.GeoOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import java.util.UUID

@ExtendWith(MockKExtension::class)
class LocationServiceTest {

    @MockK
    lateinit var redisTemplate: RedisTemplate<String, Any>

    @InjectMockKs
    lateinit var locationService: LocationService


    @Test
    @DisplayName("pingLocation: успех при наличии ключа в Redis")
    fun pingLocation_Success() {
        // Arrange
        val driverId = UUID.randomUUID()
        val statusKey = RedisSchema.driverStatusKey(driverId)
        val point = Point(53.67, 23.83)
        val geoOps = mockk<GeoOperations<String, Any>>()

        every { redisTemplate.hasKey(statusKey) } returns true
        every { redisTemplate.opsForGeo() } returns geoOps
        every { geoOps.add(RedisSchema.DRIVER_LOCATIONS_KEY, point, driverId.toString()) } returns 1L
        every { redisTemplate.expire(statusKey, any<Duration>()) } returns true

        // Act
        locationService.pingLocation(driverId, point)

        // Assert
        verify(exactly = 1) { geoOps.add(any(), any(), driverId.toString()) }
        verify(exactly = 1) { redisTemplate.expire(statusKey, Duration.ofMinutes(30)) }
    }

    @Test
    @DisplayName("pingLocation: ошибка DriverNotFoundException, если ключа нет в Redis")
    fun pingLocation_ThrowsException_WhenKeyNotFound() {
        // Arrange
        val driverId = UUID.randomUUID()
        val statusKey = RedisSchema.driverStatusKey(driverId)
        val point = Point(53.67, 23.83)
        every { redisTemplate.hasKey(statusKey) } returns false

        // Act
        val exception = assertThrows<DriverNotFoundException> {
            locationService.pingLocation(driverId, point)
        }
        // Assert
        assertEquals("driver not found", exception.message)
        verify(exactly = 0) { redisTemplate.opsForGeo() }
    }

    @Test
    @DisplayName("updateSession: успешное сохранение статуса")
    fun updateSession_Success() {
        // Arrange
        val driverId = UUID.randomUUID()
        val statusKey = RedisSchema.driverStatusKey(driverId)
        val status = WorkStatus.AVAILABLE
        val valueOps = mockk<ValueOperations<String, Any>>()

        every { redisTemplate.opsForValue() } returns valueOps
        every { valueOps.set(statusKey, status, any<Duration>()) } returns Unit

        // Act
        locationService.updateSession(driverId, status)

        // Assert
        verify(exactly = 1) { valueOps.set(statusKey, status, Duration.ofMinutes(30)) }
    }

    @Test
    @DisplayName("deleteSession: успешное удаление ключей")
    fun deleteSession_Success() {
        // Arrange
        val driverId = UUID.randomUUID()
        val statusKey = RedisSchema.driverStatusKey(driverId)
        val geoOps = mockk<GeoOperations<String, Any>>()
        every { redisTemplate.delete(statusKey) } returns true
        every { redisTemplate.opsForGeo() } returns geoOps
        every { geoOps.remove(RedisSchema.DRIVER_LOCATIONS_KEY, *anyVararg()) } returns 1L
        // Act
        locationService.deleteSession(driverId)

        // Assert
        verify(exactly = 1) { redisTemplate.delete(statusKey) }
        verify(exactly = 1) { geoOps.remove(RedisSchema.DRIVER_LOCATIONS_KEY, *anyVararg()) }
    }
}