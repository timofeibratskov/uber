package com.example.driver_service.config

import com.example.driver_service.dto.DriverNotification
import com.example.driver_service.dto.DriverRatingEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class KafkaConfig {

    private fun commonConsumerProps(groupId: String): Map<String, Any> = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:29092",
        ConsumerConfig.GROUP_ID_CONFIG to groupId,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false
    )

    @Bean
    fun driverNotificationConsumerFactory(): ConsumerFactory<String, DriverNotification> {
        return DefaultKafkaConsumerFactory(
            commonConsumerProps("driver-service-group"),
            StringDeserializer(),
            JsonDeserializer(DriverNotification::class.java).apply {
                addTrustedPackages("com.example.driver_service.dto")
            }
        )
    }

    @Bean
    fun driverNotificationListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, DriverNotification> {
        return ConcurrentKafkaListenerContainerFactory<String, DriverNotification>().apply {
            consumerFactory = driverNotificationConsumerFactory()
        }
    }

    @Bean
    fun driverRatingConsumerFactory(): ConsumerFactory<String, DriverRatingEvent> {
        return DefaultKafkaConsumerFactory(
            commonConsumerProps("driver-rating-group"),
            StringDeserializer(),
            JsonDeserializer(DriverRatingEvent::class.java).apply {
                addTrustedPackages("com.example.driver_service.dto")
            }
        )
    }

    @Bean
    fun driverRatingListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, DriverRatingEvent> {
        return ConcurrentKafkaListenerContainerFactory<String, DriverRatingEvent>().apply {
            consumerFactory = driverRatingConsumerFactory()
        }
    }
}