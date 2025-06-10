package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeekBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ShiftPlanningValidatorImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ValidationErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShiftPlanningValidatorTest {

    private ShiftPlanningValidatorImpl validator;

    @BeforeEach
    void setup() {
        validator = new ShiftPlanningValidatorImpl();
    }

    @Test
    void testValidateDescriptions_emptyAndTooLong() {
        ShiftBlueprint empty = new ShiftBlueprint.Builder()
            .withDescription(" ")
            .build();

        ShiftBlueprint tooLong = new ShiftBlueprint.Builder()
            .withDescription("x".repeat(101))
            .build();

        ShiftBlueprint valid = new ShiftBlueprint.Builder()
            .withDescription("Valid")
            .build();

        Optional<ValidationErrors> result = validator.validateDescriptions(List.of(empty, tooLong, valid));
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getValidationErrors().size());
    }

    @Test
    void testValidateManpowerMinimum_zeroIsInvalid() {
        ShiftBlueprint invalid = new ShiftBlueprint.Builder()
            .withDescription("Test")
            .withManPower(0)
            .build();

        ShiftBlueprint valid = new ShiftBlueprint.Builder()
            .withDescription("Test")
            .withManPower(1)
            .build();

        Optional<ValidationErrors> result = validator.validateManpowerMinimum(List.of(invalid, valid));
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getValidationErrors().size());
    }

    @Test
    void testValidateWeeklyDurationsPerPlan_invalidTotalDuration() {
        ShiftDayBlueprint d1 = new ShiftDayBlueprint.Builder()
            .withDay(DayOfWeek.MONDAY)
            .withStartTime(LocalTime.of(8, 0))
            .withDuration(Duration.ofHours(7))
            .build();

        ShiftWeekBlueprint week = new ShiftWeekBlueprint.Builder()
            .withIndex(0)
            .withDays(List.of(d1))
            .build();

        ShiftBlueprint shift = new ShiftBlueprint.Builder()
            .withDescription("Test")
            .withManPower(1)
            .build();

        shift.addWeeks(List.of(week));

        PlanBlueprint plan = new PlanBlueprint();
        plan.setId(1L);

        Optional<ValidationErrors> result = validator.validateWeeklyDurationsPerPlan(List.of(shift), plan);
        assertTrue(result.isPresent());
        assertTrue(result.get().getValidationErrors().get(0).contains("instead of required 40h"));
    }

    @Test
    void testValidateConsistentWeekCountPerPlan_differentCounts() {
        ShiftBlueprint s1 = new ShiftBlueprint.Builder().build();
        ShiftWeekBlueprint w1 = new ShiftWeekBlueprint.Builder().withIndex(0).withDays(List.of()).build();
        s1.addWeeks(List.of(w1));

        ShiftBlueprint s2 = new ShiftBlueprint.Builder().build();
        ShiftWeekBlueprint w2a = new ShiftWeekBlueprint.Builder().withIndex(0).withDays(List.of()).build();
        ShiftWeekBlueprint w2b = new ShiftWeekBlueprint.Builder().withIndex(1).withDays(List.of()).build();
        s2.addWeeks(List.of(w2a, w2b));

        PlanBlueprint plan = new PlanBlueprint();
        plan.setId(1L);
        s1.setPlan(plan);
        s2.setPlan(plan);

        Optional<ValidationErrors> result = validator.validateConsistentWeekCountPerPlan(List.of(s1, s2), plan);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getValidationErrors().size());
    }

    @Test
    void testValidateOverlappingShiftsPerPlan_detectsOverlap() {
        ShiftDayBlueprint a = new ShiftDayBlueprint.Builder()
            .withDay(DayOfWeek.MONDAY)
            .withStartTime(LocalTime.of(8, 0))
            .withDuration(Duration.ofHours(4))
            .build();

        ShiftDayBlueprint b = new ShiftDayBlueprint.Builder()
            .withDay(DayOfWeek.MONDAY)
            .withStartTime(LocalTime.of(11, 0))
            .withDuration(Duration.ofHours(3))
            .build();

        ShiftWeekBlueprint week1 = new ShiftWeekBlueprint.Builder().withIndex(0).withDays(List.of(a)).build();
        ShiftWeekBlueprint week2 = new ShiftWeekBlueprint.Builder().withIndex(0).withDays(List.of(b)).build();

        ShiftBlueprint s1 = new ShiftBlueprint.Builder().withDescription("A").build();
        s1.addWeeks(List.of(week1));

        ShiftBlueprint s2 = new ShiftBlueprint.Builder().withDescription("B").build();
        s2.addWeeks(List.of(week2));

        PlanBlueprint plan = new PlanBlueprint();
        plan.setId(1L);
        s1.setPlan(plan);
        s2.setPlan(plan);

        Optional<ValidationErrors> result = validator.validateOverlappingShiftsPerPlan(List.of(s1, s2), plan);
        assertTrue(result.isPresent(), "Expected overlap conflict but found none.");
        assertTrue(result.get().getValidationErrors().get(0).contains("overlap"));
    }
}
