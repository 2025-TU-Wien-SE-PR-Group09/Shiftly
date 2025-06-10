package at.ac.tuwien.sepr.groupphase.backend.logic;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeekBlueprint;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RotatingShiftSlot {

    private final ShiftBlueprint blueprint;
    private final ShiftWeekBlueprint week;
    private final int manpower;

    public final Deque<ApplicationUser> assignedUsers = new ArrayDeque<>();

    RotatingShiftSlot previous;
    RotatingShiftSlot next;

    public RotatingShiftSlot(ShiftBlueprint blueprint, ShiftWeekBlueprint week) {
        this.blueprint = blueprint;
        this.week = week;
        this.manpower = blueprint.getManPower();
    }

    public void setPrevious(RotatingShiftSlot previous) {
        this.previous = previous;
    }

    public void setNext(RotatingShiftSlot next) {
        this.next = next;
    }

    public int rotate() {
        if (assignedUsers.isEmpty()) {
            return 0;
        }
        int extraRotationsPerformed = 0;

        for (int i = 0; i < Math.min(manpower, next.manpower); i++) {
            if (assignedUsers.isEmpty()) {
                break;
            }
            ApplicationUser user = assignedUsers.removeLast();
            if (next.manpower <= next.assignedUsers.size()) {
                // this means we need to rotate the next to make space
                if (!fillFromPrevious(user)) {
                    extraRotationsPerformed += next.pureRotateOne();
                    next.assignedUsers.addFirst(user);
                }
                continue;
            }
            next.assignedUsers.addFirst(user);
        }

        while (!assignedUsers.isEmpty() && assignedUsers.size() < manpower) {
            ApplicationUser overflow = assignedUsers.removeLast();
            if (!fillFromPrevious(overflow)) {
                break;
            }
        }
        return extraRotationsPerformed;
    }

    private int pureRotateOne() {
        if (assignedUsers.isEmpty()) {
            return 0;
        }
        int extraRotationsPerformed = 1;
        ApplicationUser user = assignedUsers.removeLast();
        if (next.manpower <= next.assignedUsers.size()) {
            extraRotationsPerformed += next.pureRotateOne();
        }
        next.assignedUsers.addFirst(user);
        return extraRotationsPerformed;
    }

    private boolean fillFromPrevious(ApplicationUser user) {
        RotatingShiftSlot target = this.previous;
        while (target != null && target != this) {
            if (target.assignedUsers.size() < target.manpower) {
                target.assignedUsers.addLast(user);
                return true;
            }
            target = target.previous;
        }
        return false;
    }

    public List<ShiftDayBlueprint> getDays() {
        return week.getDays();
    }

    public String description() {
        return blueprint.getDescription() + "-W" + week.getWeekIndex();
    }

    @Override
    public String toString() {
        String name = description();
        String prevName = previous != null ? previous.description() : "null";
        String nextName = next != null ? next.description() : "null";

        return String.format(
            "[%s | manpower=%d | users=%s | prev=%s | next=%s]",
            name,
            manpower,
            assignedUsers.stream().map(ApplicationUser::getEmail).collect(Collectors.toList()),
            prevName,
            nextName
        );
    }

    public ShiftBlueprint getBlueprint() {
        return blueprint;
    }

    public ShiftWeekBlueprint getWeek() {
        return week;
    }

    public int getManpower() {
        return manpower;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RotatingShiftSlot that)) {
            return false;
        }
        return Objects.equals(blueprint, that.blueprint)
            && Objects.equals(week, that.week);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blueprint, week);
    }
}
