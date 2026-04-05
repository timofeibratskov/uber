package com.example.driver_service.service

import com.example.driver_service.constant.RedisSchema
import com.example.driver_service.exception.DriverNotFoundException
import com.example.driver_service.model.enums.WorkStatus
import java.time.Duration
import java.util.UUID
import mu.KotlinLogging
import org.springframework.data.geo.Point
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class LocationService(
    private val redisTemplate: RedisTemplate<String, Any>,
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun pingLocation(id: UUID, point: Point) {
        if (!redisTemplate.hasKey(RedisSchema.driverStatusKey(id))) {
            throw DriverNotFoundException("driver not found").also {
                log.error { "driver not found with id: $id" }
            }
        }
        redisTemplate.opsForGeo().add(RedisSchema.DRIVER_LOCATIONS_KEY, point, id.toString())
        redisTemplate.expire(RedisSchema.driverStatusKey(id), Duration.ofMinutes(30))
        log.info("driver: $id is ping location: ${point.x}, ${point.y}")
    }

    fun updateSession(id: UUID, status: WorkStatus) {
        redisTemplate.opsForValue().set(
            RedisSchema.driverStatusKey(id),
            status,
            Duration.ofMinutes(30)
        )
    }

    fun deleteSession(id: UUID) {
        redisTemplate.delete(RedisSchema.driverStatusKey(id))
        redisTemplate.opsForGeo().remove(RedisSchema.DRIVER_LOCATIONS_KEY, id)
    }
}