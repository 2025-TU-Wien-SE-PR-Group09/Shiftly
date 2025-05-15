package at.ac.tuwien.sepr.groupphase.backend.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.DayOfWeek;

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
