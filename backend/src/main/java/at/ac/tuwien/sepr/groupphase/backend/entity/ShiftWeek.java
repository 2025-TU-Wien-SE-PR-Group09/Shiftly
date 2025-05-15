package at.ac.tuwien.sepr.groupphase.backend.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;


@Entity
public class ShiftWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer weekIndex;

    @ManyToOne
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @OneToMany(mappedBy = "shiftWeek", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftDay> days = new ArrayList<ShiftDay>();

    public ShiftWeek() {
    }

    public ShiftWeek(Integer weekIndex, Shift shift) {
        this.weekIndex = weekIndex;
        this.shift = shift;
    }

    public List<ShiftDay> getDays() {
        return days;
    }

    public void setDays(List<ShiftDay> shiftdays) {
        this.days = shiftdays;
    }
}