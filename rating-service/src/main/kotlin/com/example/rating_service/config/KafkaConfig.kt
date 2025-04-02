package com.example.rating_service.config

import com.example.rating_service.dto.RatingIdInRideEvent
import com.example.rating_service.dto.UserRatingEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig {

    // Общая конфигурация для всех продюсеров
    private fun producerFactoryConfig(): Map<String, Any> = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "kafka:9092",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
        JsonSerializer.ADD_TYPE_INFO_HEADERS to false
    )

    @Bean
    fun ratingIdInRideProducerFactory(): ProducerFactory<String, RatingIdInRideEvent> {
        return DefaultKafkaProducerFactory(producerFactoryConfig())
    }

    @Bean
    fun userRatingProducerFactory(): ProducerFactory<String, UserRatingEvent> {
        return DefaultKafkaProducerFactory(producerFactoryConfig())
    }

    @Bean
    fun ratingIdInRideKafkaTemplate(): KafkaTemplate<String, RatingIdInRideEvent> {
        return KafkaTemplate(ratingIdInRideProducerFactory())
    }

    @Bean
    fun userRatingKafkaTemplate(): KafkaTemplate<String, UserRatingEvent> {
        return KafkaTemplate(userRatingProducerFactory())
    }
}