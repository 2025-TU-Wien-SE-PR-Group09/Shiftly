package at.ac.tuwien.sepr.groupphase.backend.entity;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

import at.ac.tuwien.sepr.groupphase.backend.entity.converter.DayOfWeekConverter;
import jakarta.persistence.*;


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

    private LocalTime startTime;

    private Duration duration;

    @ManyToOne
    @JoinColumn(name = "shift_week_id")
    private ShiftWeekBlueprint shiftWeekBlueprint;

    public ShiftDayBlueprint() {
    }

    public ShiftDayBlueprint(DayOfWeek day, LocalTime startTime, Duration duration, ShiftWeekBlueprint shiftWeekBlueprint) {
        this.day = day;
        this.startTime = startTime;
        this.duration = duration;
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
}