package com.example.ride_service.model.enums.converter;

import com.example.ride_service.model.enums.CancelInitiator;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class CancelInitiatorConverter implements AttributeConverter<CancelInitiator, String> {

    @Override
    public String convertToDatabaseColumn(CancelInitiator attribute) {
        return (attribute == null) ? null : attribute.name();
    }

    @Override
    public CancelInitiator convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        return Stream.of(CancelInitiator.values())
                .filter(c -> c.name().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown database value: " + dbData));
    }
}