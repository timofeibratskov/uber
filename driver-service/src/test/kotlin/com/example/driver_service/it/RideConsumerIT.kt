//package com.example.driver_service.it
//
//import com.example.driver_service.model.entity.CarEntity
//import com.example.driver_service.model.entity.DriverEntity
//import com.example.driver_service.model.enums.Gender
//import com.example.driver_service.model.enums.WorkStatus
//import com.example.driver_service.model.event.RideCanceledEvent
//import com.example.driver_service.repository.CarRepository
//import com.example.driver_service.repository.DriverRepository
//import com.example.driver_service.repository.OutboxEventRepository
//import com.fasterxml.jackson.databind.ObjectMapper
//import java.time.Duration
//import java.time.LocalDateTime
//import java.util.UUID
//import java.util.concurrent.TimeUnit
//import kotlin.test.assertEquals
//import kotlin.test.assertNotNull
//import org.apache.kafka.clients.producer.ProducerRecord
//import org.awaitility.Awaitility.await
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.data.redis.core.RedisTemplate
//import org.springframework.kafka.core.KafkaTemplate
//import org.springframework.kafka.test.context.EmbeddedKafka
//
//
//@EmbeddedKafka(
//    partitions = 1,
//    topics = ["\${app.Kafka.ride-topic}"],
//    brokerProperties = [
//        "listeners=PLAINTEXT://localhost:0",
//        "advertised.listeners=PLAINTEXT://localhost:0"
//    ]
//)
//class RideConsumerIT @Autowired constructor(
//    private val driverRepository: DriverRepository,
//    private val carRepository: CarRepository,
//    private val redisTemplate: RedisTemplate<String, Any>,
//    private val objectMapper: ObjectMapper,
//    private val kafkaTemplate: KafkaTemplate<String, String>,
//    private val outboxEventRepository: OutboxEventRepository
//) : BaseIT() {
//
//    @Value("\${app.Kafka.ride-topic}")
//    private lateinit var topic: String
//
//    @BeforeEach
//    fun cleanTable() {
//        driverRepository.deleteAll()
//        carRepository.deleteAll()
//        outboxEventRepository.deleteAll()
//        redisTemplate.delete(redisTemplate.keys("*"))
//    }
//
//    @Test
//    @DisplayName("Консьюмер должен получить RIDE_CANCELLED,  найти водителя и сменить ему статус на  AVAILABLE")
//    fun testRideCancelEventSetDriverWorkStatus() {
//        // Arrange
//        val rideId = UUID.randomUUID()
//
//        val driverEntity = DriverEntity(
//            UUID.randomUUID(),
//            "driverName",
//            "driver@gmail.com",
//            "password",
//            "+35295555555",
//            gender = Gender.MALE,
//            workStatus = WorkStatus.BUSY
//        )
//        driverRepository.save(driverEntity)
//
//        val carEntity = CarEntity(
//            UUID.randomUUID(),
//            driverEntity.id,
//            "white",
//            "3305AM-4",
//            "BMW",
//            "3",
//            4
//        )
//
//        carRepository.save(carEntity)
//        driverEntity.carId = carEntity.id
//        driverRepository.update(driverEntity)
//
//        val event = RideCanceledEvent(
//            rideId = rideId,
//            driverId = driverEntity.id,
//            cancelAt = LocalDateTime.now()
//        )
//
//        val payload = objectMapper.writeValueAsString(event)
//
//        val record = ProducerRecord<String, String>(topic, payload)
//        record.headers().add("eventType", EventType.RIDE_CANCELED.eventName.toByteArray())
//
//        // Act
//        kafkaTemplate.send(record).get(3, TimeUnit.SECONDS)
//
//        // Assert
//        await()
//            .atMost(Duration.ofSeconds(15))
//            .pollInterval(Duration.ofMillis(500))
//            .untilAsserted {
//                val driver = driverRepository.findById(driverEntity.id)
//                assertNotNull(driver)
//                assertEquals(WorkStatus.AVAILABLE, driver.workStatus)
//            }
//    }
//}