package com.example.ride_service.it.support;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class KafkaTestSupport {
    private final String bootstrapServers;

    public KafkaTestSupport(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public KafkaConsumer<String, String> createConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        ));
    }

    public String getEventType(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader("eventType");
        return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
    }
}
