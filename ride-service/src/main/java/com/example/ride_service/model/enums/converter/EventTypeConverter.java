package com.example.ride_service.model.enums.converter;

import com.example.ride_service.model.enums.EventType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class EventTypeConverter implements AttributeConverter<EventType, String> {

    @Override
    public String convertToDatabaseColumn(EventType eventType) {
        if (eventType == null) throw new IllegalArgumentException("eventType cannot be null");
        return eventType.getEventName();
    }

    @Override
    public EventType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        return Stream.of(EventType.values())
                .filter(c -> c.getEventName().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown database value: " + dbData));
    }
}