package at.ac.tuwien.sepr.groupphase.backend.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;

/**
 * This class is a JPA converter that converts the DayOfWeek enum to a String for database storage
 * and vice versa.
 * It is marked with @Converter(autoApply = true) to apply it automatically to all DayOfWeek fields.
 */
@Converter(autoApply = true)
public class DayOfWeekConverter implements AttributeConverter<DayOfWeek, String> {

    @Override
    public String convertToDatabaseColumn(DayOfWeek attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public DayOfWeek convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DayOfWeek.valueOf(dbData);
    }
}
