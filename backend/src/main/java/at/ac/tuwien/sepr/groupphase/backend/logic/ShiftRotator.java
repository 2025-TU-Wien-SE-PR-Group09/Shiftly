package at.ac.tuwien.sepr.groupphase.backend.logic;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class ShiftRotator {

    private final Queue<RotatingShiftSlot> rotationQueue = new LinkedList<>();
    private final List<RotatingShiftSlot> slotsInRotationOrder;
    private final List<RotatingShiftSlot> slotsInNaturalOrder;
    private InitialShiftAssignmentStrategy initialShiftAssignmentStrategy;
    private final int shifts;

    public static final InitialShiftAssignmentStrategy FAIR_GREEDY = (slots, users) -> {
        if (users.isEmpty() || slots.isEmpty()) return;

        List<ApplicationUser> userPool = new ArrayList<>(users);
        int userIndex = 0;

        int totalAssignments = Math.min(
            userPool.size(),
            slots.stream().mapToInt(RotatingShiftSlot::getManpower).sum()
        );

        int assigned = 0;
        int slotIndex = 0;

        while (assigned < totalAssignments) {
            RotatingShiftSlot slot = slots.get(slotIndex % slots.size());

            if (slot.assignedUsers.size() < slot.getManpower()) {
                slot.assignedUsers.add(userPool.get(userIndex));
                assigned++;
                userIndex++;
            }

            slotIndex++;
        }
    };
    public static final InitialShiftAssignmentStrategy FILL_ASSIGNMENT = (slots, users) -> {

        var totalManPower = slots.stream().mapToInt(RotatingShiftSlot::getManpower).sum();

        Iterator<ApplicationUser> userIterator = users.stream().limit(totalManPower).iterator();

        for (RotatingShiftSlot slot : slots) {
            for (int i = 0; i < slot.getManpower() && userIterator.hasNext(); i++) {
                slot.assignedUsers.add(userIterator.next());
            }
        }
    };


    public ShiftRotator(PlanBlueprint blueprint) {

        if (blueprint.getShifts().isEmpty()) {
            throw new IllegalArgumentException("Blueprint must contain at least one shift.");
        }

        this.initialShiftAssignmentStrategy = FAIR_GREEDY;
        this.shifts = blueprint.getShifts().size();

        List<RotatingShiftSlot> allSlotsBase = blueprint.getShifts().stream()
            .flatMap(shift -> shift.getShiftWeeks().stream()
                .map(week -> new RotatingShiftSlot(shift, week))).toList();

        this.slotsInRotationOrder = allSlotsBase.stream()
            .sorted(Comparator
                .comparingInt((RotatingShiftSlot slot) -> slot.getWeek().getWeekIndex())
                .thenComparing(x -> x.getWeek().getDays().stream()
                    .map(ShiftDayBlueprint::getStartTime)
                    .min(Comparator.naturalOrder())
                    .orElse(LocalTime.MAX)
                ))
            .toList();

        this.slotsInNaturalOrder = allSlotsBase.stream()
            .sorted(Comparator.<RotatingShiftSlot, LocalTime>comparing(x -> x.getWeek().getDays().stream()
                    .map(ShiftDayBlueprint::getStartTime)
                    .min(Comparator.naturalOrder())
                    .orElse(LocalTime.MAX))
                .thenComparingInt(x -> x.getWeek().getWeekIndex()))
            .collect(Collectors.toList());

        linkSlots();
    }

    public ShiftRotator(PlanBlueprint blueprint, InitialShiftAssignmentStrategy initialShiftAssignmentStrategy) {
        this(blueprint);
        this.initialShiftAssignmentStrategy = initialShiftAssignmentStrategy;
    }

    private void linkSlots() {
        int size = slotsInNaturalOrder.size();
        for (int i = 0; i < size; i++) {
            RotatingShiftSlot current = slotsInNaturalOrder.get(i);
            RotatingShiftSlot next = slotsInNaturalOrder.get((i + 1) % size);
            RotatingShiftSlot prev = slotsInNaturalOrder.get((i - 1 + size) % size);

            current.setNext(next);
            current.setPrevious(prev);

            rotationQueue.add(slotsInRotationOrder.get(i));
        }
    }

    public void assignInitialUsers(List<ApplicationUser> users) {
        var slots = slotsInRotationOrder.stream().limit(slotsInRotationOrder.size()).toList();
        this.initialShiftAssignmentStrategy.assign(slots, users);
    }

    public List<CurrentShiftSlot> getNextCycle() {
        List<CurrentShiftSlot> result = new ArrayList<>();
        var iter = rotationQueue.iterator();
        for (int i = 0; i < shifts; i++) {
            RotatingShiftSlot slot = iter.next();
            assert slot != null;
            result.add(new CurrentShiftSlot(slot.getBlueprint(), slot.getWeek(), new ArrayList<>(slot.assignedUsers)));
        }

        for (int i = 0; i < shifts; i++) {
            RotatingShiftSlot slot = rotationQueue.poll();
            assert slot != null;
            // in cases like [user1]->[user2,user3] when the current slot we want to rotate is [user1] we need to rotate the user1 to the next slot
            // so slot.rotate() will also rotate the next slot so user1 has a chance to be assigned to the next slot
            // .rotate() will return the number of slots to skip in the rotation queue
            i += slot.rotate();
            rotationQueue.add(slot);
        }
        return result;
    }

    public record CurrentShiftSlot(ShiftBlueprint blueprint, ShiftWeekBlueprint week,
                                   List<ApplicationUser> assignedUsers) {
    }
}
