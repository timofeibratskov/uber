package com.example.driver_service.config

import com.example.driver_service.model.event.DriverAssignedEvent
import com.example.driver_service.model.event.DriverSearchingEvent
import com.example.driver_service.model.event.NoDriversEvent
import com.example.driver_service.model.event.RideCanceledEvent
import com.example.driver_service.model.event.RideCompletedEvent
import com.example.driver_service.model.event.UserRegisteredEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventConfig(
    @Value("\${spring.kafka.event.driver-searching}") private val driverSearching: String,
    @Value("\${spring.kafka.event.ride-canceled}") private val rideCanceled: String,
    @Value("\${spring.kafka.event.driver-assigned}") private val driverAssigned: String,
    @Value("\${spring.kafka.event.ride-completed}") private val rideCompleted: String,
    @Value("\${spring.kafka.event.no-drivers}") private val noDrivers: String,
    @Value("\${spring.kafka.event.user-created}") private val userCreated: String
) {

    @Bean("kafkaTypeMapping")
    fun eventTypeMapping(): Map<String, Class<out Any>> {
        val map = HashMap<String, Class<out Any>>()

        map[driverSearching] = DriverSearchingEvent::class.java
        map[rideCanceled] = RideCanceledEvent::class.java
        map[rideCompleted] = RideCompletedEvent::class.java
        map[driverAssigned] = DriverAssignedEvent::class.java
        map[noDrivers] = NoDriversEvent::class.java
        map[userCreated] = UserRegisteredEvent::class.java

        return map
    }
}
