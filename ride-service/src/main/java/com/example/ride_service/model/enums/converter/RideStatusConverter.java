package com.example.ride_service.model.enums.converter;

import com.example.ride_service.model.enums.RideStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class RideStatusConverter implements AttributeConverter<RideStatus, String> {

    @Override
    public String convertToDatabaseColumn(RideStatus status) {
        if (status == null) throw new IllegalArgumentException("RideStatus cannot be null");
        return status.name();
    }

    @Override
    public RideStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) throw new IllegalArgumentException("Database value for RideStatus cannot be null");
        return Stream.of(RideStatus.values())
                .filter(status -> status.name().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown RideStatus value: " + dbData
                ));
    }
}