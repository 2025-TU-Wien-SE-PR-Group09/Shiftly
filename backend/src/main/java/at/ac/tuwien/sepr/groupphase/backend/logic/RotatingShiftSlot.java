package at.ac.tuwien.sepr.groupphase.backend.logic;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeekBlueprint;

import java.util.*;
import java.util.stream.Collectors;

public class RotatingShiftSlot {

    private final ShiftBlueprint blueprint;
    private final ShiftWeekBlueprint week;
    private int manpower;

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


    public void rotateSimple(int min) {
        if (assignedUsers.isEmpty()) {
            return;
        }

        int assigned = assignedUsers.size();

        for (int i = 0; i < Math.min(min, assigned); i++) {
            ApplicationUser user = assignedUsers.removeLast();
            if (next.assignedUsers.size() >= next.manpower) {
                next.rotateSimple(1);
            }
            next.assignedUsers.addFirst(user);
        }
    }

    public boolean trySqueeze(ApplicationUser user) {
        if (assignedUsers.size() < manpower) {
            assignedUsers.addFirst(user);
            return true;
        }

        return false;
    }


    public List<ApplicationUser> rotate() {
        if (assignedUsers.isEmpty()) {
            return List.of();
        }

        List<ApplicationUser> overflow = new ArrayList<>();

        for (int i = 0; i < manpower; i++) {
            if (assignedUsers.isEmpty()) {
                break;
            }
            ApplicationUser user = assignedUsers.removeLast();
            if (next.assignedUsers.size() < next.manpower) {
                next.assignedUsers.addFirst(user);
                continue;
            }
            overflow.add(user);
        }

        return overflow;
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

    public List<ApplicationUser> getAssignedUsers() {
        return new ArrayList<>(assignedUsers);
    }

    public void setManpower(int manpower) {
        this.manpower = manpower;
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
