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
    private final int weeksPerShift;
    private final int minimumManpower;


    public ShiftRotator(PlanBlueprint blueprint) {

        if (blueprint.getShifts().isEmpty()) {
            throw new IllegalArgumentException("Blueprint must contain at least one shift.");
        }

        this.initialShiftAssignmentStrategy = Strategy.FAIR_GREEDY;
        this.shifts = blueprint.getShifts().size();
        this.weeksPerShift = blueprint.getShifts().stream()
            .mapToInt(shift -> shift.getShiftWeeks().size())
            .max()
            .orElseThrow(() -> new IllegalArgumentException("No weeks found in shifts."));
        this.minimumManpower = blueprint.getShifts().stream()
            .mapToInt(ShiftBlueprint::getManPower)
            .min()
            .orElseThrow(() -> new IllegalArgumentException("No manpower defined in shifts."));

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
        alignManpowerPerShiftToFirstWeekAssignments();
    }

    private void alignManpowerPerShiftToFirstWeekAssignments() {
        Map<ShiftBlueprint, Integer> assignedPerShift = new HashMap<>();

        for (RotatingShiftSlot slot : slotsInRotationOrder) {
            if (slot.getWeek().getWeekIndex() == 0 && !assignedPerShift.containsKey(slot.getBlueprint())) {
                assignedPerShift.put(slot.getBlueprint(), slot.assignedUsers.size());
            }
        }

        for (RotatingShiftSlot slot : slotsInRotationOrder) {
            Integer newManpower = assignedPerShift.get(slot.getBlueprint());
            if (newManpower != null) {
                slot.setManpower(newManpower);
            }
        }
    }

    public List<CurrentShiftSlot> getNextCycle() {
        List<CurrentShiftSlot> result = new ArrayList<>();
        var iter = rotationQueue.iterator();
        for (int i = 0; i < shifts; i++) {
            RotatingShiftSlot slot = iter.next();
            assert slot != null;
            result.add(new CurrentShiftSlot(slot.getBlueprint(), slot.getWeek(), new ArrayList<>(slot.assignedUsers)));
        }

        var cycle = new ArrayList<RotatingShiftSlot>();
        for (int i = 0; i < shifts; i++) {
            RotatingShiftSlot slot = rotationQueue.poll();
            assert slot != null;
            cycle.add(slot);
            rotationQueue.add(slot);
        }

        if (weeksPerShift == 1) {
            cycle.getFirst().rotateSimple(minimumManpower);
            return result;
        }

        var overflow = new ArrayList<ApplicationUser>();
        cycle.forEach(slot -> overflow.addAll(slot.rotate()));

        for (ApplicationUser user : overflow) {
            for (RotatingShiftSlot slot : cycle.stream().map(c -> c.next).toList()) {
                if (slot.trySqueeze(user)) {
                    break;
                }
            }
        }


        return result;
    }

    public record CurrentShiftSlot(ShiftBlueprint blueprint, ShiftWeekBlueprint week,
                                   List<ApplicationUser> assignedUsers) {
    }

    public static class Strategy {
        public static final InitialShiftAssignmentStrategy FAIR_GREEDY = (slots, users) -> {
            if (users.isEmpty() || slots.isEmpty()) {
                return;
            }

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

            for (RotatingShiftSlot slot : slots) {
                slot.setManpower(slot.assignedUsers.size());
            }
        };
        public static final InitialShiftAssignmentStrategy FILL_GREEDY = (slots, users) -> {

            var totalManPower = slots.stream().mapToInt(RotatingShiftSlot::getManpower).sum();

            Iterator<ApplicationUser> userIterator = users.stream().limit(totalManPower).iterator();

            for (RotatingShiftSlot slot : slots) {
                for (int i = 0; i < slot.getManpower() && userIterator.hasNext(); i++) {
                    slot.assignedUsers.add(userIterator.next());
                }
            }

            for (RotatingShiftSlot slot : slots) {
                slot.setManpower(slot.assignedUsers.size());
            }
        };
    }
}
