package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.entity.converter.DayOfWeekConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;


/*
 * This class represents a single day of a shift, including the start time and duration.
 * It is part of a ShiftWeek, which contains multiple ShiftDay entities.
 */
@Entity
@Table(name = "shift_day_blueprint")
public class ShiftDayBlueprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = DayOfWeekConverter.class)
    @Column(name = "weekday", nullable = false)
    private DayOfWeek day;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "duration", nullable = false)
    private Duration duration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shift_week_id")
    private ShiftWeekBlueprint shiftWeekBlueprint;

    public ShiftDayBlueprint() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setShiftWeekBlueprint(ShiftWeekBlueprint shiftWeekBlueprint) {
        this.shiftWeekBlueprint = shiftWeekBlueprint;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public static class Builder {
        private final ShiftDayBlueprint day = new ShiftDayBlueprint();

        public Builder withDay(DayOfWeek dayOfWeek) {
            day.setDay(dayOfWeek);
            return this;
        }

        public Builder withStartTime(LocalTime time) {
            day.setStartTime(time);
            return this;
        }

        public Builder withDuration(Duration duration) {
            day.setDuration(duration);
            return this;
        }

        public ShiftDayBlueprint build() {
            return day;
        }
    }
}