package com.example.driver_service.it

import com.example.driver_service.constant.RedisSchema
import com.example.driver_service.model.entity.CarEntity
import com.example.driver_service.model.entity.DriverEntity
import com.example.driver_service.model.enums.EventType
import com.example.driver_service.model.enums.Gender
import com.example.driver_service.model.enums.WorkStatus
import com.example.driver_service.model.event.RideCreateEvent
import com.example.driver_service.repository.CarRepository
import com.example.driver_service.repository.DriverRepository
import com.example.driver_service.repository.OutboxEventRepository
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.geo.Point
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka


@EmbeddedKafka(
    partitions = 1,
    topics = ["\${app.Kafka.ride-topic}"],
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:0",
        "port=0"
    ]
)
class RideConsumerIT @Autowired constructor(
    private val driverRepository: DriverRepository,
    private val carRepository: CarRepository,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val outboxEventRepository: OutboxEventRepository
) : BaseIT() {

    @Value("\${app.Kafka.ride-topic}")
    private lateinit var topic: String

    @BeforeEach
    fun cleanTable() {
        driverRepository.deleteAll()
        carRepository.deleteAll()
        outboxEventRepository.deleteAll()
        redisTemplate.delete(redisTemplate.keys("*"))
    }

    @Test
    @DisplayName("Консьюмер должен получить RIDE_CREATED, найти водителя и записать ASSIGNED_DRIVER в outbox")
    fun testRideCreateEventWithDriverFound() {
        // Arrange
        val point = Point(53.675434, 23.827427)

        val driverEntity = DriverEntity(
            UUID.randomUUID(),
            "driverName",
            "driver@gmail.com",
            "password",
            "+35295555555",
            gender = Gender.MALE,
            workStatus = WorkStatus.AVAILABLE
        )
        driverRepository.save(driverEntity)

        val carEntity = CarEntity(
            UUID.randomUUID(),
            driverEntity.id,
            "white",
            "3305AM-4",
            "BMW",
            "3",
            4
        )

        carRepository.save(carEntity)
        driverEntity.carId = carEntity.id
        driverRepository.update(driverEntity)

        redisTemplate.opsForGeo()
            .add(RedisSchema.DRIVER_LOCATIONS_KEY, point, driverEntity.id.toString())

        redisTemplate.opsForValue()
            .set(
                RedisSchema.driverStatusKey(driverEntity.id),
                driverEntity.workStatus,
                Duration.ofMinutes(30)
            )

        val rideId = UUID.randomUUID()

        val event = RideCreateEvent(
            rideId = rideId,
            startPoint = point,
            seats = 4
        )

        val payload = objectMapper.writeValueAsString(event)

        val record = ProducerRecord<String, String>(topic, payload)
        record.headers().add("eventType", EventType.RIDE_CREATED.eventName.toByteArray())

        // Act
        kafkaTemplate.send(record).get(3, TimeUnit.SECONDS)

        // Assert
        await()
            .atMost(Duration.ofSeconds(15))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted {
                val redisStatus = redisTemplate.opsForValue()
                    .get(RedisSchema.driverStatusKey(driverEntity.id))

                val updatedDriver = driverRepository.findById(driverEntity.id)

                assertNotNull(updatedDriver)
                assertEquals(WorkStatus.BUSY.toString(), redisStatus)
                assertEquals(WorkStatus.BUSY, updatedDriver.workStatus)

                val outbox = outboxEventRepository.findAllByOrderByCreatedAt()[0]

                assertNotNull(outbox)
                assertEquals(EventType.ASSIGNED_DRIVER.eventName, outbox.eventType!!.eventName)
                assertNotNull(outbox.createdAt)
                assertEquals(topic, outbox.topic)
                assertNotNull(outbox.payload)
                assertNotNull(outbox.id)
            }
    }

    @Test
    @DisplayName("Консьюмер должен получить RIDE_CREATED, НЕ найти водителя и записать NO_AVAILABLE_DRIVERS в outbox")
    fun testRideCreateEventWithNoDriversFound() {
        // Arrange
        val point = Point(53.675434, 23.827427)
        val rideId = UUID.randomUUID()

        val event = RideCreateEvent(
            rideId = rideId,
            startPoint = point,
            seats = 4
        )

        val payload = objectMapper.writeValueAsString(event)

        val record = ProducerRecord<String, String>(topic, payload)
        record.headers().add("eventType", EventType.RIDE_CREATED.eventName.toByteArray())

        // Act
        kafkaTemplate.send(record).get(3, TimeUnit.SECONDS)

        // Assert
        await()
            .atMost(Duration.ofSeconds(15))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted {
                val outbox = outboxEventRepository.findAllByOrderByCreatedAt()[0]

                assertNotNull(outbox)
                assertEquals(EventType.NO_AVAILABLE_DRIVERS.eventName, outbox.eventType!!.eventName)
                assertNotNull(outbox.createdAt)
                assertEquals(topic, outbox.topic)
                assertNotNull(outbox.payload)
                assertNotNull(outbox.id)
            }
    }
}