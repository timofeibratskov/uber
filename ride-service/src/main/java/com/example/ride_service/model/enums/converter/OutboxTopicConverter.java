package com.example.ride_service.model.enums.converter;

import com.example.ride_service.model.enums.TopicType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class OutboxTopicConverter implements AttributeConverter<TopicType, String> {

    @Override
    public String convertToDatabaseColumn(TopicType outboxTopic) {
        if (outboxTopic == null) throw new IllegalArgumentException("outboxTopic cannot be null");
        return outboxTopic.name();
    }

    @Override
    public TopicType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        return Stream.of(TopicType.values())
                .filter(c -> c.name().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown database value: " + dbData));
    }
}