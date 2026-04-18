package com.example.ride_service.it.support;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    public KafkaProducer<String, String> createProducer() {
        return new KafkaProducer<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(),
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(),
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.RETRIES_CONFIG, 3
        ));
    }

    public void sendWithHeader(
            String topic,
            String key,
            String payload,
            String headerKey,
            String headerValue
    ) throws ExecutionException, InterruptedException, TimeoutException {

        try (KafkaProducer<String, String> producer = createProducer()) {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic,
                    null,
                    key,
                    payload
            );
            record.headers().add(headerKey, headerValue.getBytes(StandardCharsets.UTF_8));
            producer.send(record).get(5, TimeUnit.SECONDS);
            producer.flush();
        }
    }

    public String getEventType(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader("eventType");
        return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
    }
}
