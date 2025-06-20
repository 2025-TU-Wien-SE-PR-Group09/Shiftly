package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeekBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.logic.RotatingShiftSlot;
import at.ac.tuwien.sepr.groupphase.backend.logic.ShiftRotator;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InitialShiftAssignmentStrategyTests {

    private RotatingShiftSlot createSlot(String name, int manpower) {
        ShiftDayBlueprint day = new ShiftDayBlueprint.Builder()
            .withDay(DayOfWeek.MONDAY)
            .withStartTime(LocalTime.of(8, 0))
            .withDuration(Duration.ofHours(8))
            .build();

        ShiftWeekBlueprint week = new ShiftWeekBlueprint.Builder()
            .withIndex(0)
            .withDay(d -> d.withDay(day.getDay()).withStartTime(day.getStartTime()).withDuration(day.getDuration()))
            .build();

        ShiftBlueprint shift = new ShiftBlueprint.Builder()
            .withDescription(name)
            .withManPower(manpower)
            .addWeek(week)
            .build();

        return new RotatingShiftSlot(shift, week);
    }

    private List<ApplicationUser> createUsers(int count) {
        List<ApplicationUser> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ApplicationUser user = new ApplicationUser();
            user.setEmail("user" + i + "@test.local");
            users.add(user);
        }
        return users;
    }

    // FILL_GREEDY TESTS

    @Test
    void fillGreedyShouldFillSlotsInOrderUntilFull() {
        var strategy = ShiftRotator.Strategy.FILL_GREEDY;

        var slots = List.of(
            createSlot("SlotA", 2),
            createSlot("SlotB", 1)
        );
        var users = createUsers(3);

        strategy.assign(slots, users);

        assertThat(slots.get(0).getAssignedUsers()).containsExactlyInAnyOrder(users.get(0), users.get(1));
        assertThat(slots.get(1).getAssignedUsers()).containsExactlyInAnyOrder(users.get(2));
    }

    @Test
    void fillGreedyShouldRespectManpowerLimitAndIgnoreExcessUsers() {
        var strategy = ShiftRotator.Strategy.FILL_GREEDY;

        var slots = List.of(createSlot("Slot", 1));
        var users = createUsers(5);

        strategy.assign(slots, users);

        assertThat(slots.getFirst().getAssignedUsers()).hasSize(1);
    }

    @Test
    void fillGreedyShouldLeaveEmptySlotsIfUsersAreExhausted() {
        var strategy = ShiftRotator.Strategy.FILL_GREEDY;

        var slots = List.of(
            createSlot("SlotA", 2),
            createSlot("SlotB", 2)
        );
        var users = createUsers(2);

        strategy.assign(slots, users);

        assertThat(slots.get(0).getAssignedUsers()).hasSize(2);
        assertThat(slots.get(1).getAssignedUsers()).isEmpty();
    }

    // FAIR_GREEDY TESTS

    @Test
    void fairGreedyShouldDistributeOneByOne() {
        var strategy = ShiftRotator.Strategy.FAIR_GREEDY;

        var slots = List.of(
            createSlot("SlotA", 2),
            createSlot("SlotB", 2)
        );
        var users = createUsers(3);

        strategy.assign(slots, users);

        var allAssigned = new ArrayList<>();
        slots.forEach(s -> allAssigned.addAll(s.getAssignedUsers()));
        assertThat(allAssigned).containsExactlyInAnyOrderElementsOf(users);

        assertThat(slots.stream().filter(s -> !s.getAssignedUsers().isEmpty()).count()).isEqualTo(2);
    }

    @Test
    void fairGreedyShouldLeaveNoEmptySlotIfPossible() {
        var strategy = ShiftRotator.Strategy.FAIR_GREEDY;

        var slots = List.of(
            createSlot("A", 2),
            createSlot("B", 2),
            createSlot("C", 2)
        );
        var users = createUsers(4);

        strategy.assign(slots, users);

        long nonEmpty = slots.stream().filter(s -> !s.getAssignedUsers().isEmpty()).count();
        assertThat(nonEmpty).isEqualTo(3);
    }

    @Test
    void fairGreedyShouldRespectManpowerLimits() {
        var strategy = ShiftRotator.Strategy.FAIR_GREEDY;

        var slots = List.of(createSlot("S1", 1), createSlot("S2", 1));
        var users = createUsers(10);

        strategy.assign(slots, users);

        assertThat(slots.get(0).getAssignedUsers()).hasSize(1);
        assertThat(slots.get(1).getAssignedUsers()).hasSize(1);
    }
}
