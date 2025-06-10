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
    public Optional<ValidationErrors> validateDayStructuresPerDepartment(List<ShiftBlueprint> shifts) {

        ValidationErrors errors = new ValidationErrors();

        Map<DayOfWeek, List<ShiftDayBlueprint>> groupedByDay = new HashMap<>();

        for (ShiftBlueprint shift : shifts) {
            for (ShiftWeekBlueprint week : shift.getShiftWeeks()) {
                for (ShiftDayBlueprint day : week.getDays()) {
                    System.out.println("   Tag: " + day.getDay() + ", Start: " + day.getStartTime() + ", Dauer: " + day.getDuration());
                    groupedByDay.computeIfAbsent(day.getDay(), k -> new ArrayList<>()).add(day);
                }
            }
        }


        for (var entry : groupedByDay.entrySet()) {
            DayOfWeek day = entry.getKey();
            List<ShiftDayBlueprint> dayShifts = entry.getValue();

            dayShifts.sort(Comparator.comparing(ShiftDayBlueprint::getStartTime));

            Duration reference = null;
            for (int i = 0; i < dayShifts.size(); i++) {
                ShiftDayBlueprint a = dayShifts.get(i);
                LocalTime startA = a.getStartTime();
                LocalTime endA = startA.plus(a.getDuration());


                if (reference == null) {
                    reference = a.getDuration();
                }

                if (i + 1 < dayShifts.size()) {
                    ShiftDayBlueprint b = dayShifts.get(i + 1);
                    LocalTime startB = b.getStartTime();
                    LocalTime endB = startB.plus(b.getDuration());
                    if (startA.isBefore(endB) && endA.isAfter(startB)) {
                        errors.add("Overlapping shifts on " + day + ": "
                            + startA + "–" + endA + " overlaps with " + startB + "–" + endB);
                    }
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
    public Optional<ValidationErrors> validateWeek(ShiftWeekBlueprintDto shiftWeekBlueprintDto) {
        return Optional.empty();
    }

    @Override
    public Optional<ValidationErrors> validateDay(ShiftDayBlueprint shiftWeekBlueprintDto) {
        return Optional.empty();
    }

    @Override
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
                    errors.add("Mismatch in week " + index + ": Found shift with "
                        + d.toHours() + "h, expected " + reference.toHours() + "h");
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






}
