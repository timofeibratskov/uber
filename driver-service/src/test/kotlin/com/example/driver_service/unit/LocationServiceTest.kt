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
import java.time.Duration
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.geo.Distance
import org.springframework.data.geo.GeoResult
import org.springframework.data.geo.GeoResults
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.GeoOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.data.redis.domain.geo.GeoReference

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

    @Test
    @DisplayName("getAvailableIds: успешный поиск и фильтрация доступных водителей")
    fun getAvailableIds_Success() {
        // Arrange
        val searchPoint = Point(53.67, 23.83)

        val driverId1 = UUID.randomUUID()
        val driverId2 = UUID.randomUUID()

        val location1 = RedisGeoCommands.GeoLocation<Any>(driverId1, Point(53.1, 23.1))
        val location2 = RedisGeoCommands.GeoLocation<Any>(driverId2, Point(53.2, 23.2))

        val res1 = GeoResult(location1, Distance(1.0, Metrics.KILOMETERS))
        val res2 = GeoResult(location2, Distance(2.0, Metrics.KILOMETERS))

        val geoResults = GeoResults(listOf(res1, res2))

        val geoOps = mockk<GeoOperations<String, Any>>()
        val valueOps = mockk<ValueOperations<String, Any>>()

        every { redisTemplate.opsForGeo() } returns geoOps
        every { redisTemplate.opsForValue() } returns valueOps

        every {
            geoOps.search(
                any<String>(),
                any<GeoReference<Any>>(),
                any<Distance>(),
                any<RedisGeoCommands.GeoSearchCommandArgs>()
            )
        } returns geoResults

        every { valueOps.get(RedisSchema.driverStatusKey(driverId1)) } returns "AVAILABLE"
        every { valueOps.get(RedisSchema.driverStatusKey(driverId2)) } returns "BUSY"

        // Act
        val result = locationService.getAvailableIds(searchPoint)

        // Assert
        assertEquals(1, result.size)
        assertEquals(driverId1, result[0])
        verify(exactly = 1) {
            geoOps.search(
                any<String>(),
                any<GeoReference<Any>>(),
                any<Distance>(),
                any<RedisGeoCommands.GeoSearchCommandArgs>()
            )
        }
        verify(exactly = 2) { valueOps.get(any()) }
    }

    @Test
    @DisplayName("getAvailableIds: возврат пустого списка, если Redis ничего не нашел")
    fun getAvailableIds_EmptyResults() {
        val searchPoint = Point(53.67, 23.83)
        val geoOps = mockk<GeoOperations<String, Any>>()

        every { redisTemplate.opsForGeo() } returns geoOps
        every {
            geoOps.search(
                any<String>(),
                any<GeoReference<Any>>(),
                any<Distance>(),
                any<RedisGeoCommands.GeoSearchCommandArgs>()
            )
        } returns null

        val result = locationService.getAvailableIds(searchPoint)

        assertEquals(0, result.size)
    }
}