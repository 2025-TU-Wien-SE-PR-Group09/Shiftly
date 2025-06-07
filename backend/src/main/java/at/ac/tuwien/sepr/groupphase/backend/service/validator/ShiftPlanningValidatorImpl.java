package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeekBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekBlueprintDto;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShiftPlanningValidatorImpl implements ShiftPlanningValidator {

    @Override
    public Optional<ValidationErrors> validateOverlappingShiftsPerPlan(List<ShiftBlueprint> shifts, PlanBlueprint plan) {
        ValidationErrors errors = new ValidationErrors();

        record GroupKey(DayOfWeek day, Long planId, int weekIndex) {}

        Map<GroupKey, List<ShiftDayBlueprint>> grouped = new HashMap<>();

        for (ShiftBlueprint shift : shifts) {
            Long planId = (shift.getPlan() != null && shift.getPlan().getId() != null)
                ? shift.getPlan().getId()
                : plan.getId();

            for (ShiftWeekBlueprint week : shift.getShiftWeeks()) {
                int weekIndex = week.getWeekIndex();
                for (ShiftDayBlueprint day : week.getDays()) {
                    GroupKey key = new GroupKey(day.getDay(), planId, weekIndex);
                    grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(day);
                }
            }
        }

        for (var entry : grouped.entrySet()) {
            GroupKey key = entry.getKey();
            List<ShiftDayBlueprint> dayShifts = entry.getValue();
            dayShifts.sort(Comparator.comparing(ShiftDayBlueprint::getStartTime));

            for (int i = 0; i < dayShifts.size() - 1; i++) {
                ShiftDayBlueprint a = dayShifts.get(i);
                ShiftDayBlueprint b = dayShifts.get(i + 1);

                LocalTime startA = a.getStartTime();
                LocalTime endA = startA.plus(a.getDuration());
                LocalTime startB = b.getStartTime();
                LocalTime endB = startB.plus(b.getDuration());

                long startAsec = startA.toSecondOfDay();
                long endAsec = endA.toSecondOfDay();
                if (endAsec <= startAsec) {
                    endAsec += 24 * 3600;
                }

                long startBsec = startB.toSecondOfDay();
                long endBsec = endB.toSecondOfDay();
                if (endBsec <= startBsec) {
                    endBsec += 24 * 3600;
                }

                if (startAsec < endBsec && endAsec > startBsec) {
                    String message = String.format(
                        "Shift overlap on %s: A shift from %s to %s overlaps with another from %s to %s in this plan.",
                        key.day(), startA, endA, startB, endB
                    );
                    errors.add(message);
                }
            }
        }

        return errors.isValid() ? Optional.empty() : Optional.of(errors);
    }







    private String format(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        return (hours > 0 ? hours + "h" : "") + (minutes > 0 ? " " + minutes + "m" : "").trim();
    }


    @Override
    public Optional<ValidationErrors> validatePlan(PlanBlueprint planBlueprint) {
        return Optional.empty();
    }

    @Override
    public Optional<ValidationErrors> validateShift(ShiftBlueprint shiftBlueprint) {
        return Optional.empty();
    }

    @Override
    public Optional<ValidationErrors> validateWeek(ShiftWeekBlueprint shiftWeekBlueprint) {
        return Optional.empty();
    }

    @Override
    public Optional<ValidationErrors> validateDay(ShiftDayBlueprint shiftWeekBlueprintDto) {
        return Optional.empty();
    }

    /*  @Override
    public Optional<ValidationErrors> validateWeeklyDurationsPerPlan(List<ShiftBlueprint> shifts, PlanBlueprint plan) {
        ValidationErrors errors = new ValidationErrors();


        List<ShiftBlueprint> relevantShifts = shifts.stream()
            .filter(s -> plan.equals(s.getPlan()) || s.getPlan() == null)
            .toList();


        Map<Integer, List<Duration>> weekDurations = new HashMap<>();

        for (ShiftBlueprint shift : relevantShifts) {
            for (ShiftWeekBlueprint week : shift.getShiftWeeks()) {
                int index = week.getWeekIndex();
                Duration total = week.getDays().stream()
                    .map(ShiftDayBlueprint::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);

                weekDurations.computeIfAbsent(index, k -> new ArrayList<>()).add(total);
            }
        }


        for (var entry : weekDurations.entrySet()) {
            Integer index = entry.getKey();
            List<Duration> durations = entry.getValue();

            Duration reference = durations.get(0);
            for (Duration d : durations) {
                if (!reference.minus(d).abs().isZero()) {
                    errors.add(String.format(
                        "Inconsistent weekly shift duration: Expected %dh but found %dh in one of the shifts.",
                        reference.toHours(), d.toHours()
                    ));
                }
            }
        }

        return errors.isValid() ? Optional.empty() : Optional.of(errors);
    }
    */

    @Override
    public Optional<ValidationErrors> validateWeeklyDurationsPerPlan(List<ShiftBlueprint> shifts, PlanBlueprint plan) {
        ValidationErrors errors = new ValidationErrors();

        for (ShiftBlueprint shift : shifts) {
            for (ShiftWeekBlueprint week : shift.getShiftWeeks()) {
                Duration total = week.getDays().stream()
                    .map(ShiftDayBlueprint::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);

                if (!total.equals(Duration.ofHours(40))) {
                    String message = String.format(
                        "Shift '%s' has a week with total duration of %dh %dm instead of required 40h.",
                        shift.getDescription(),
                        total.toHours(),
                        total.toMinutesPart()
                    );
                    errors.add(message);
                }
            }
        }

        return errors.isValid() ? Optional.empty() : Optional.of(errors);
    }


    @Override
    public Optional<ValidationErrors> validateConsistentWeekCountPerPlan(List<ShiftBlueprint> shifts, PlanBlueprint targetPlan) {
        ValidationErrors errors = new ValidationErrors();

        List<ShiftBlueprint> planShifts = shifts.stream()
            .filter(s -> s.getPlan() == null || s.getPlan().getId().equals(targetPlan.getId()))
            .toList();


        int expectedWeekCount = planShifts.stream()
            .mapToInt(s -> s.getShiftWeeks().size())
            .max()
            .orElse(0);

        for (ShiftBlueprint shift : planShifts) {
            int actualWeekCount = shift.getShiftWeeks().size();
            if (actualWeekCount != expectedWeekCount) {
                errors.add("Week count of newly added shift '"
                    + "' does not match existing shifts in the same plan.");
            }
        }

        return errors.isValid() ? Optional.empty() : Optional.of(errors);
    }

    @Override
    public Optional<ValidationErrors> validateDescriptions(List<ShiftBlueprint> shifts) {
        ValidationErrors errors = new ValidationErrors();

        for (ShiftBlueprint shift : shifts) {
            String description = shift.getDescription();
            if (description == null || description.trim().isEmpty()) {
                errors.add("Description must not be empty.");
            } else if (description.length() > 100) {
                errors.add("Description is too long (max 100 characters).");
            }
        }

        return errors.isValid() ? Optional.empty() : Optional.of(errors);
    }

    @Override
    public Optional<ValidationErrors> validateManpowerMinimum(List<ShiftBlueprint> shifts) {
        ValidationErrors errors = new ValidationErrors();

        for (ShiftBlueprint shift : shifts) {
            if (shift.getManPower() < 1) {
                errors.add("Shift \"" + shift.getDescription() + "\" must have at least one required person.");
            }
        }

        return errors.isValid() ? Optional.empty() : Optional.of(errors);
    }








}
