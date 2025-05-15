package at.ac.tuwien.sepr.groupphase.backend.entity;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

import at.ac.tuwien.sepr.groupphase.backend.entity.converter.DayOfWeekConverter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GenerationType;


@Entity
public class ShiftDay {

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
    private ShiftWeek shiftWeek;

    public ShiftDay() {
    }

    public ShiftDay(DayOfWeek day, LocalTime startTime, Duration duration, ShiftWeek shiftWeek) {
        this.day = day;
        this.startTime = startTime;
        this.duration = duration;
        this.shiftWeek = shiftWeek;
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