package at.ac.tuwien.sepr.groupphase.backend.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Month;

@Converter(autoApply = true)
public class MonthConverter implements AttributeConverter<Month, String> {
    @Override
    public String convertToDatabaseColumn(Month attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public Month convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Month.valueOf(dbData);
    }
}