package com.example.passenger_service.model.enums.converter;

import com.example.passenger_service.model.enums.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, String> {
    @Override
    public String convertToDatabaseColumn(Gender gender) {
        return (gender == null) ? Gender.OTHER.name() : gender.name();
    }

    @Override
    public Gender convertToEntityAttribute(String dbData) {
        return (dbData == null) ? Gender.OTHER : Gender.valueOf(dbData);
    }
}
