package com.example.driver_service.service

import com.example.driver_service.constant.RedisSchema
import com.example.driver_service.exception.DriverNotFoundException
import com.example.driver_service.model.enums.WorkStatus
import java.time.Duration
import java.util.UUID
import mu.KotlinLogging
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.domain.geo.GeoReference
import org.springframework.data.redis.domain.geo.Metrics
import org.springframework.stereotype.Service

@Service
class LocationService(
    private val redisTemplate: RedisTemplate<String, Any>,
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    fun pingLocation(id: UUID, point: Point) {
        redisTemplate.hasKey(RedisSchema.driverStatusKey(id))?.let {
            if (!it) {
                throw DriverNotFoundException("driver not found").also {
                    log.error { "driver not found with id: $id" }
                }
            }
        }
        redisTemplate.opsForGeo().add(RedisSchema.DRIVER_LOCATIONS_KEY, point, id.toString())
        redisTemplate.expire(RedisSchema.driverStatusKey(id), Duration.ofMinutes(30))
        log.info { "driver: $id is ping location: ${point.x}, ${point.y}" }
    }

    fun updateSession(id: UUID, status: WorkStatus) {
        redisTemplate.opsForValue().set(
            RedisSchema.driverStatusKey(id),
            status,
            Duration.ofMinutes(30)
        )
        log.info { "driver: $id is updated session: $status" }
    }

    fun deleteSession(id: UUID) {
        redisTemplate.delete(RedisSchema.driverStatusKey(id))
        redisTemplate.opsForGeo().remove(RedisSchema.DRIVER_LOCATIONS_KEY, id)
        log.info { "driver: $id is deleted session" }
    }

    fun getAvailableIds(point: Point): List<UUID> {
        log.info { "Searching for drivers id within 5km radius of point: $point" }

        val results = redisTemplate.opsForGeo()
            .search(
                RedisSchema.DRIVER_LOCATIONS_KEY,
                GeoReference.fromCoordinate(point),
                Distance(5.0, Metrics.KILOMETERS),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                    .includeDistance()
                    .sortAscending()
            )

        val candidates = results?.content

        if (candidates.isNullOrEmpty()) {
            log.info { "No drivers found in the specified radius for point: $point" }
            return emptyList()
        }

        log.debug { "Found ${candidates.size} candidates in Geo-index. Filtering by status..." }

        return candidates.stream()
            .map { it.content.name.toString() }
            .map { UUID.fromString(it) }
            .filter { id ->
                val status = redisTemplate.opsForValue().get(RedisSchema.driverStatusKey(id))
                status?.toString() == WorkStatus.AVAILABLE.name
            }
            .toList()
    }
}