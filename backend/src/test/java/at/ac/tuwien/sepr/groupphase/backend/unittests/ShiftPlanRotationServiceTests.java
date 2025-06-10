package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.TimeService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.shift.ShiftPlanRotationServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.shift.ShiftPlanRotationService;
import io.jsonwebtoken.lang.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShiftPlanRotationServiceTests {

    private ShiftPlanRotationService serviceUnderTest;
    private  TimeService timeService;
    private ConcreteShiftPlanRepository concreteShiftPlanRepository;
    private  ShiftAssignmentAuditLogRepository shiftAssignmentAuditLogRepository;

    @BeforeEach
    void setUp() {
        timeService = mock(TimeService.class);
        when(timeService.now()).thenReturn(LocalDateTime.of(2025,6,8,22,0));
        concreteShiftPlanRepository = mock(ConcreteShiftPlanRepository.class);
        shiftAssignmentAuditLogRepository = mock(ShiftAssignmentAuditLogRepository.class);


        serviceUnderTest = new ShiftPlanRotationServiceImpl(
            timeService,
            concreteShiftPlanRepository,
            shiftAssignmentAuditLogRepository);
    }

    // Plan: EarlyWeek1:[X] -> LateWeek1:[X]
    // Initial: [user1]-> [user2]
    @Test
    void rotatePlanWithTwoShiftsOneWeekSingleManPower(){
        //act

        var department = new Department();

        var bluePrint = new PlanBlueprint.Builder()
            .withDescription("This plan has early shift and late shift, one weeks per shift. Just 1 worker per shift")
            .withDepartment(department)
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Early Shift")
                .withManPower(1)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(6,0)).build()
                    ).build()
                ).build()
            )
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Late Shift")
                .withManPower(1)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(14,0)).build()
                    ).build()
                ).build()
            )
            .build();

        LocalDate startDate = LocalDate.of(2025, 6, 9);
        LocalDate endDate = startDate.plusWeeks(12);

        var concretePlan = new ConcreteShiftPlan.Builder()
            .withDepartment(department)
            .withStartDate(startDate)
            .withEndDate(endDate);

        var user1 = new ApplicationUser();
        user1.setEmail("user1@shift.local");
        var user2 = new ApplicationUser();
        user2.setEmail("user2@shift.local");


        List<ApplicationUser> employees = List.of(user1, user2);
        when(concreteShiftPlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        //act
        var result = serviceUnderTest.generateRotatingPlan(concretePlan.build(), bluePrint, employees);

        //assert
        assertAll(
            () -> Assert.notNull(result),
            () -> Assert.notNull(result.getScheduledShifts()),
            () -> Assert.isTrue(result.getScheduledShifts().size() == 24, "There should be 24 Shifts generated"),
           () ->{
                for (int week = 0; week < 12; week++) {
                    LocalDate weekStart = startDate.plusWeeks(week);

                    var weekShifts = result.getScheduledShifts().stream()
                        .filter(s -> s.getStart().toLocalDate().equals(weekStart))
                        .toList();

                    Assert.isTrue(weekShifts.size() == 2);

                    var early = weekShifts.stream().filter(s -> s.getDescription() .equals("Early Shift")).findFirst().orElseThrow();
                    var late = weekShifts.stream().filter(s -> s.getDescription() .equals("Late Shift")).findFirst().orElseThrow();

                    Assert.isTrue(early.getAssignments().size() == 1, "Early Shift should have exactly 1 assignment");
                    Assert.isTrue(late.getAssignments().size() == 1, "Late Shift should have exactly 1 assignment");

                    switch (week % 2) {
                        case 0 -> {
                            assertShift(early, user1);
                            assertShift(late, user2);
                        }
                        case 1 -> {
                            assertShift(early, user2);
                            assertShift(late, user1);
                        }
                    }
                }
            }
        );
    }

    // Plan: EarlyWeek1:[X] -> LateWeek1:[X, X]
    // Initial: [user1]-> [user2, user3]
    @Test
    void rotatePlanWithTwoShiftsOneWeekDifferentManPower(){
        //act

        var department = new Department();

        var bluePrint = new PlanBlueprint.Builder()
            .withDescription("This plan has early shift and late shift, two weeks per shift. Different manpower")
            .withDepartment(department)
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Early Shift")
                .withManPower(1)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(6,0)).build()
                    ).build()
                ).build()
            )
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Late Shift")
                .withManPower(2)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(14,0)).build()
                    ).build()
                ).build()
            )
            .build();

        LocalDate startDate = LocalDate.of(2025, 6, 9);
        LocalDate endDate = startDate.plusWeeks(12);

        var concretePlan = new ConcreteShiftPlan.Builder()
            .withDepartment(department)
            .withStartDate(startDate)
            .withEndDate(endDate);

        var user1 = new ApplicationUser();
        user1.setEmail("user1@shift.local");
        var user2 = new ApplicationUser();
        user2.setEmail("user2@shift.local");
        var user3 = new ApplicationUser();
        user3.setEmail("user3@shift.local");


        List<ApplicationUser> employees = List.of(user1, user2, user3);
        when(concreteShiftPlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        //act
        var result = serviceUnderTest.generateRotatingPlan(concretePlan.build(), bluePrint, employees);

        //assert
        assertAll(
            () -> Assert.notNull(result),
            () -> Assert.notNull(result.getScheduledShifts()),
            () -> Assert.isTrue(result.getScheduledShifts().size() == 24, "There should be 24 Shifts generated"),
            () ->{
                for (int week = 0; week < 12; week++) {
                    LocalDate weekStart = startDate.plusWeeks(week);

                    var weekShifts = result.getScheduledShifts().stream()
                        .filter(s -> s.getStart().toLocalDate().equals(weekStart))
                        .toList();

                    Assert.isTrue(weekShifts.size() == 2);

                    var early = weekShifts.stream().filter(s -> s.getDescription() .equals("Early Shift")).findFirst().orElseThrow();
                    var late = weekShifts.stream().filter(s -> s.getDescription() .equals("Late Shift")).findFirst().orElseThrow();

                    Assert.isTrue(early.getAssignments().size() == 1, "Early Shift should have exactly 1 assignment");
                    Assert.isTrue(late.getAssignments().size() == 2, "Late Shift should have exactly 2 assignment");


                    switch (week % 3 ){
                        case 0 -> {
                            assertShift(early, user1);
                            assertShift(late, user2, user3);
                        }
                        case 1 -> {
                            assertShift(early, user3);
                            assertShift(late, user1, user2);
                        }
                        case 2 -> {
                            assertShift(early, user2);
                            assertShift(late, user3, user1);
                        }
                    }
                }
            }
        );
    }
    // Plan: EarlyWeek1:[X,X] -> LateWeek1:[X, X]
    // Initial: [user1,user2]-> [user3, user4]
    @Test
    void rotatePlanWithTwoShiftsOneWeekSameManPower2(){
        //act

        var department = new Department();

        var bluePrint = new PlanBlueprint.Builder()
            .withDescription("This plan has early shift and late shift, one weeks per shift. Enought manpower")
            .withDepartment(department)
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Early Shift")
                .withManPower(2)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(6,0)).build()
                    ).build()
                ).build()
            )
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Late Shift")
                .withManPower(2)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(14,0)).build()
                    ).build()
                ).build()
            )
            .build();

        LocalDate startDate = LocalDate.of(2025, 6, 9);
        LocalDate endDate = startDate.plusWeeks(12);

        var concretePlan = new ConcreteShiftPlan.Builder()
            .withDepartment(department)
            .withStartDate(startDate)
            .withEndDate(endDate);

        var user1 = new ApplicationUser();
        user1.setEmail("user1@shift.local");
        var user2 = new ApplicationUser();
        user2.setEmail("user2@shift.local");
        var user3 = new ApplicationUser();
        user3.setEmail("user3@shift.local");
        var user4 = new ApplicationUser();
        user4.setEmail("user4@shift.local");


        List<ApplicationUser> employees = List.of(user1, user2, user3, user4);
        when(concreteShiftPlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        //act
        var result = serviceUnderTest.generateRotatingPlan(concretePlan.build(), bluePrint, employees);

        //assert
        assertAll(
            () -> Assert.notNull(result),
            () -> Assert.notNull(result.getScheduledShifts()),
            () -> Assert.isTrue(result.getScheduledShifts().size() == 24, "There should be 24 Shifts generated"),
            () ->{
                for (int week = 0; week < 12; week++) {
                    LocalDate weekStart = startDate.plusWeeks(week);

                    var weekShifts = result.getScheduledShifts().stream()
                        .filter(s -> s.getStart().toLocalDate().equals(weekStart))
                        .toList();

                    Assert.isTrue(weekShifts.size() == 2);

                    var early = weekShifts.stream().filter(s -> s.getDescription() .equals("Early Shift")).findFirst().orElseThrow();
                    var late = weekShifts.stream().filter(s -> s.getDescription() .equals("Late Shift")).findFirst().orElseThrow();

                    Assert.isTrue(early.getAssignments().size() == 2, "Early Shift should have exactly 2 assignment");
                    Assert.isTrue(late.getAssignments().size() == 2, "Late Shift should have exactly 2 assignment");

                    switch (week % 2){
                        case 0 -> {
                            assertShift(early, user1, user2);
                            assertShift(late, user3, user4);
                        }
                        case  1 -> {
                            assertShift(early, user3, user4);
                            assertShift(late, user1, user2);
                        }
                    }
                }
            }
        );
    }

    // Plan: EarlyWeek1:[X,X] -> MidWeek1:[X,X] -> LateWeek1:[X, X]
    // Initial: [user1,user2]-> [user3, user4] -> [user5, user6]
    @Test
    void rotatePlanWithThreeShiftsOneWeekSameManPower2(){
        //act

        var department = new Department();

        var bluePrint = new PlanBlueprint.Builder()
            .withDescription("This plan has early shift and late shift, one weeks per shift. Same manpower")
            .withDepartment(department)
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Early Shift")
                .withManPower(2)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(6,0)).build()
                    ).build()
                ).build()
            ).
            addShift(new ShiftBlueprint.Builder()
                .withDescription("Mid Shift")
                .withManPower(2)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(10,0)).build()
                    ).build()
                ).build()
            )
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Late Shift")
                .withManPower(2)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(14,0)).build()
                    ).build()
                ).build()
            )
            .build();

        LocalDate startDate = LocalDate.of(2025, 6, 9);
        LocalDate endDate = startDate.plusWeeks(12);

        var concretePlan = new ConcreteShiftPlan.Builder()
            .withDepartment(department)
            .withStartDate(startDate)
            .withEndDate(endDate);

        var user1 = new ApplicationUser();
        user1.setEmail("user1@shift.local");
        var user2 = new ApplicationUser();
        user2.setEmail("user2@shift.local");
        var user3 = new ApplicationUser();
        user3.setEmail("user3@shift.local");
        var user4 = new ApplicationUser();
        user4.setEmail("user4@shift.local");
        var user5 = new ApplicationUser();
        user5.setEmail("user5@shift.local");
        var user6 = new ApplicationUser();
        user6.setEmail("user6@shift.local");


        List<ApplicationUser> employees = List.of(user1, user2, user3, user4, user5, user6);
        when(concreteShiftPlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        //act
        var result = serviceUnderTest.generateRotatingPlan(concretePlan.build(), bluePrint, employees);

        //assert
        assertAll(
            () -> Assert.notNull(result),
            () -> Assert.notNull(result.getScheduledShifts()),
            () -> Assert.isTrue(result.getScheduledShifts().size() == 36, "There should be 36 Shifts generated"),
            () ->{
                for (int week = 0; week < 12; week++) {
                    LocalDate weekStart = startDate.plusWeeks(week);

                    var weekShifts = result.getScheduledShifts().stream()
                        .filter(s -> s.getStart().toLocalDate().equals(weekStart))
                        .toList();

                    Assert.isTrue(weekShifts.size() == 3);

                    var early = weekShifts.stream().filter(s -> s.getDescription() .equals("Early Shift")).findFirst().orElseThrow();
                    var mid = weekShifts.stream().filter(s -> s.getDescription() .equals("Mid Shift")).findFirst().orElseThrow();
                    var late = weekShifts.stream().filter(s -> s.getDescription() .equals("Late Shift")).findFirst().orElseThrow();

                    Assert.isTrue(early.getAssignments().size() == 2, "Early Shift should have exactly 2 assignment");
                    Assert.isTrue(mid.getAssignments().size() == 2, "Mid Shift should have exactly 2 assignment");
                    Assert.isTrue(late.getAssignments().size() == 2, "Late Shift should have exactly 2 assignment");

                    switch (week % 3){
                        case 0 -> {
                            assertShift(early, user1, user2);
                            assertShift(mid, user3, user4);
                            assertShift(late, user5, user6);
                        }
                        case  1 -> {
                            assertShift(early, user5, user6);
                            assertShift(mid, user1, user2);
                            assertShift(late, user3, user4);
                        }
                        case 2 ->{
                            assertShift(early, user3, user4);
                            assertShift(mid, user5, user6);
                            assertShift(late, user1, user2);
                        }
                    }
                }
            }
        );
    }

    // Plan: EarlyWeek1:[X] -> MidWeek1:[X] -> LateWeek1:[X]
    // Initial: [user1]-> [user2] -> [user3]
    @Test
    void rotatePlanWithThreeShiftsOneWeekSameManPower(){

        var department = new Department();

        var bluePrint = new PlanBlueprint.Builder()
            .withDescription("This plan has early shift, mid shift and late shift, one week per shift. Same manpower")
            .withDepartment(department)
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Early Shift")
                .withManPower(1)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(6,0)).build()
                    ).build()
                )
                .build()
            )
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Mid Shift")
                .withManPower(1)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(10,0)).build()
                    ).build()
                )
                .build()
            )
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Late Shift")
                .withManPower(1)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(14,0)).build()
                    ).build()
                )
                .build()
            )
            .build();

        LocalDate startDate = LocalDate.of(2025, 6, 9);
        LocalDate endDate = startDate.plusWeeks(12);

        var concretePlan = new ConcreteShiftPlan.Builder()
            .withDepartment(department)
            .withStartDate(startDate)
            .withEndDate(endDate);

        var user1 = new ApplicationUser();
        user1.setEmail("user1@shift.local");
        var user2 = new ApplicationUser();
        user2.setEmail("user2@shift.local");
        var user3 = new ApplicationUser();
        user3.setEmail("user3@shift.local");

        List<ApplicationUser> employees = List.of(user1, user2, user3);
        when(concreteShiftPlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        //act
        var result = serviceUnderTest.generateRotatingPlan(concretePlan.build(), bluePrint, employees);

        //assert
        assertAll(
            () -> Assert.notNull(result),
            () -> Assert.notNull(result.getScheduledShifts()),
            () -> Assert.isTrue(result.getScheduledShifts().size() == 36, "There should be 36 Shifts generated"),
            () ->{
                for (int week = 0; week < 12; week++) {
                    LocalDate weekStart = startDate.plusWeeks(week);

                    var weekShifts = result.getScheduledShifts().stream()
                        .filter(s -> s.getStart().toLocalDate().equals(weekStart))
                        .toList();

                    Assert.isTrue(weekShifts.size() == 3, "There should be 3 shifts per week");

                    var early = weekShifts.stream().filter(s -> s.getDescription() .equals("Early Shift")).findFirst().orElseThrow();
                    var mid = weekShifts.stream().filter(s -> s.getDescription() .equals("Mid Shift")).findFirst().orElseThrow();
                    var late = weekShifts.stream().filter(s -> s.getDescription() .equals("Late Shift")).findFirst().orElseThrow();

                    Assert.isTrue(early.getAssignments().size() == 1, "Early Shift should have exactly 1 assignment");
                    Assert.isTrue(mid.getAssignments().size() == 1, "Mid Shift should have exactly 1 assignment");
                    Assert.isTrue(late.getAssignments().size() == 1, "Late Shift should have exactly 1 assignment");


                    switch (week % 3 ){
                        case 0 -> {
                            assertShift(early, user1);
                            assertShift(mid, user2);
                            assertShift(late, user3);
                        }
                        case 1 -> {
                            assertShift(early, user3);
                            assertShift(mid, user1);
                            assertShift(late, user2);
                        }
                        case 2 -> {
                            assertShift(early, user2);
                            assertShift(mid, user3);
                            assertShift(late, user1);
                        }
                    }
                }
            }
        );
    }

    // Plan: EarlyWeek1:[X,X] ->EarlyWeek2:[X,X] -> LateWeek1:[X, X] -> LateWeek1:[X, X]
    // Initial: [user1,user2]-> [user3, user4]
    @Test
    void rotatePlanWithTwoShiftsTwoWeekSameManPower(){
        //act

        var department = new Department();

        var bluePrint = new PlanBlueprint.Builder()
            .withDescription("This plan has early shift and late shift, two weeks per shift. Same manpower")
            .withDepartment(department)
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Early Shift")
                .withManPower(2)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(6,0)).build()
                    ).build()
                ).addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(1)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(6,0)).build()
                    ).build()
                )
                .build()
            )
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Late Shift")
                .withManPower(2)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(14,0)).build()
                    ).build()
                )
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(1)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(14,0)).build()
                    ).build()
                )
                .build()
            )
            .build();

        LocalDate startDate = LocalDate.of(2025, 6, 9);
        LocalDate endDate = startDate.plusWeeks(12);

        var concretePlan = new ConcreteShiftPlan.Builder()
            .withDepartment(department)
            .withStartDate(startDate)
            .withEndDate(endDate);

        var user1 = new ApplicationUser();
        user1.setEmail("user1@shift.local");
        var user2 = new ApplicationUser();
        user2.setEmail("user2@shift.local");
        var user3 = new ApplicationUser();
        user3.setEmail("user3@shift.local");
        var user4 = new ApplicationUser();
        user4.setEmail("user4@shift.local");


        List<ApplicationUser> employees = List.of(user1, user2, user3, user4);
        when(concreteShiftPlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        //act
        var result = serviceUnderTest.generateRotatingPlan(concretePlan.build(), bluePrint, employees);

        //assert
        assertAll(
            () -> Assert.notNull(result),
            () -> Assert.notNull(result.getScheduledShifts()),
            () -> Assert.isTrue(result.getScheduledShifts().size() == 24, "There should be 24 Shifts generated"),
            () ->{
                for (int week = 0; week < 12; week++) {
                    LocalDate weekStart = startDate.plusWeeks(week);

                    var weekShifts = result.getScheduledShifts().stream()
                        .filter(s -> s.getStart().toLocalDate().equals(weekStart))
                        .toList();

                    Assert.isTrue(weekShifts.size() == 2);

                    var early = weekShifts.stream().filter(s -> s.getDescription() .equals("Early Shift")).findFirst().orElseThrow();
                    var late = weekShifts.stream().filter(s -> s.getDescription() .equals("Late Shift")).findFirst().orElseThrow();

                    Assert.isTrue(early.getAssignments().size() == 2, "Early Shift should have exactly 2 assignment");
                    Assert.isTrue(late.getAssignments().size() == 2, "Late Shift should have exactly 2 assignment");


                    // Every 2 weeks, alternate assigned user group

                    switch (week % 4) {
                        case 0, 1 -> {
                          assertShift(early, user1, user2);
                          assertShift(late, user3, user4);
                        }
                        case 2, 3 -> {
                           assertShift(early, user3, user4);
                           assertShift(late, user1, user2);
                        }
                    }
                }
            }
        );
    }

    // Plan: EarlyWeek1:[X] -> EarlyWeek2:[X] -> LateWeek1:[X, X] -> LateWeek2:[X, X]
    // Initial: [user1]-> [] -> [user2, user3] -> [ , ]
    @Test
    void rotatePlanWithTwoShiftsTwoWeekDifferentManPower(){
                //act

        var department = new Department();

        var bluePrint = new PlanBlueprint.Builder()
            .withDescription("This plan has early shift and late shift, two weeks per shift. Different manpower")
            .withDepartment(department)
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Early Shift")
                .withManPower(1)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(6,0)).build()
                    ).build()
                ).addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(1)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(6,0)).build()
                    ).build()
                )
                .build()
            )
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Late Shift")
                .withManPower(2)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(14,0)).build()
                    ).build()
                )
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(1)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(14,0)).build()
                    ).build()
                )
                .build()
            )
            .build();

        LocalDate startDate = LocalDate.of(2025, 6, 9);
        LocalDate endDate = startDate.plusWeeks(12);

        var concretePlan = new ConcreteShiftPlan.Builder()
            .withDepartment(department)
            .withStartDate(startDate)
            .withEndDate(endDate);

        var user1 = new ApplicationUser();
        user1.setEmail("user1@shift.local");
        var user2 = new ApplicationUser();
        user2.setEmail("user2@shift.local");
        var user3 = new ApplicationUser();
        user3.setEmail("user3@shift.local");


        List<ApplicationUser> employees = List.of(user1, user2, user3);
        when(concreteShiftPlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        //act
        var result = serviceUnderTest.generateRotatingPlan(concretePlan.build(), bluePrint, employees);

        //assert
        assertAll(
            () -> Assert.notNull(result),
            () -> Assert.notNull(result.getScheduledShifts()),
            () -> Assert.isTrue(result.getScheduledShifts().size() == 24, "There should be 24 Shifts generated"),
            () ->{
                for (int week = 0; week < 12; week++) {
                    LocalDate weekStart = startDate.plusWeeks(week);

                    var weekShifts = result.getScheduledShifts().stream()
                        .filter(s -> s.getStart().toLocalDate().equals(weekStart))
                        .toList();

                    Assert.isTrue(weekShifts.size() == 2);

                    var early = weekShifts.stream().filter(s -> s.getDescription() .equals("Early Shift")).findFirst().orElseThrow();
                    var late = weekShifts.stream().filter(s -> s.getDescription() .equals("Late Shift")).findFirst().orElseThrow();

                    Assert.isTrue(early.getAssignments().size() == 1, "Early Shift should have exactly 1 assignment");
                    Assert.isTrue(late.getAssignments().size() == 2, "Late Shift should have exactly 2 assignment");


                    switch (week % 6 ){
                        case 0,1 -> {
                            assertShift(early, user1);
                            assertShift(late, user2, user3);
                        }
                        case 2,3 -> {
                            assertShift(early, user3);
                            assertShift(late, user1, user2);
                        }
                        case 4,5 -> {
                            assertShift(early, user2);
                            assertShift(late, user3, user1);
                        }
                    }
                }
            }
        );
    }

    // Plan: EarlyWeek1:[X,X] -> EarlyWeek2:[X,X] -> EarlyWeek3:[X,X] -> MidWeek1:[X] -> MidWeek2:[X] -> MidWeek3:[X] -> LateWeek1:[X, X] -> LateWeek2:[X, X] -> LateWeek3:[X, X]
    // Initial: [user1]-> [] -> [user2, user3] -> [ , ]
    @Test
    void rotatePlanWithThreeShiftsTwoWeeksDifferentManPower(){

        var department = new Department();

        var bluePrint = new PlanBlueprint.Builder()
            .withDescription("This plan has early shift, mid shift and late shift, two weeks per shift. Different manpower")
            .withDepartment(department)
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Early Shift")
                .withManPower(2)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(6,0)).build()
                    ).build()
                ).addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(1)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(6,0)).build()
                    ).build()
                )
                .build()
            )
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Mid Shift")
                .withManPower(1)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(10,0)).build()
                    ).build()
                )
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(1)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(10,0)).build()
                    ).build()
                )
                .build()
            )
            .addShift(new ShiftBlueprint.Builder()
                .withDescription("Late Shift")
                .withManPower(2)
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(0)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(14,0)).build()
                    ).build()
                )
                .addWeek(new ShiftWeekBlueprint.Builder()
                    .withIndex(1)
                    .withDay( dayBuilder -> dayBuilder
                        .withDay(DayOfWeek.MONDAY)
                        .withDuration(Duration.ofHours(8))
                        .withStartTime(LocalTime.of(14,0)).build()
                    ).build()
                )
                .build()
            )
            .build();

        LocalDate startDate = LocalDate.of(2025, 6, 9);
        LocalDate endDate = startDate.plusWeeks(12);

        var concretePlan = new ConcreteShiftPlan.Builder()
            .withDepartment(department)
            .withStartDate(startDate)
            .withEndDate(endDate);

        var user1 = new ApplicationUser();
        user1.setEmail("user1@shift.local");
        var user2 = new ApplicationUser();
        user2.setEmail("user2@shift.local");
        var user3 = new ApplicationUser();
        user3.setEmail("user3@shift.local");
        var user4 = new ApplicationUser();
        user4.setEmail("user4@shift.local");
        var user5 = new ApplicationUser();
        user5.setEmail("user5@shift.local");

        List<ApplicationUser> employees = List.of(user1, user2, user3, user4, user5);
        when(concreteShiftPlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        //act
        var result = serviceUnderTest.generateRotatingPlan(concretePlan.build(), bluePrint, employees);

        //assert
        assertAll(
            () -> Assert.notNull(result),
            () -> Assert.notNull(result.getScheduledShifts()),
            () -> Assert.isTrue(result.getScheduledShifts().size() == 36, "There should be 36 Shifts generated"),
            () ->{
                for (int week = 0; week < 12; week++) {
                    LocalDate weekStart = startDate.plusWeeks(week);

                    var weekShifts = result.getScheduledShifts().stream()
                        .filter(s -> s.getStart().toLocalDate().equals(weekStart))
                        .toList();

                    Assert.isTrue(weekShifts.size() == 3, "There should be 3 shifts per week");

                    var early = weekShifts.stream().filter(s -> s.getDescription() .equals("Early Shift")).findFirst().orElseThrow();
                    var mid = weekShifts.stream().filter(s -> s.getDescription() .equals("Mid Shift")).findFirst().orElseThrow();
                    var late = weekShifts.stream().filter(s -> s.getDescription() .equals("Late Shift")).findFirst().orElseThrow();

                    Assert.isTrue(early.getAssignments().size() == 2, "Early Shift should have exactly 2 assignment");
                    Assert.isTrue(mid.getAssignments().size() == 1, "Mid Shift should have exactly 1 assignment");
                    Assert.isTrue(late.getAssignments().size() == 2, "Late Shift should have exactly 2 assignment");


                    switch (week % 10 ){
                        case 0,1 -> {
                            assertShift(early, user1, user2);
                            assertShift(mid, user3);
                            assertShift(late, user4, user5);
                        }
                        case 2,3 -> {
                            assertShift(early, user5, user1);
                            assertShift(mid, user2);
                            assertShift(late, user3, user4);
                        }
                        case 4,5 -> {
                            assertShift(early, user4, user5);
                            assertShift(mid, user1);
                            assertShift(late, user2, user3);
                        }
                        case 6,7 -> {
                            assertShift(early, user3, user4);
                            assertShift(mid, user5);
                            assertShift(late, user1, user2);
                        }
                        case 8,9 -> {
                            assertShift(early, user2, user3);
                            assertShift(mid, user4);
                            assertShift(late, user5, user1);
                        }
                    }
                }
            }
        );
    }

    private void assertShift(ScheduledShift shift, ApplicationUser... actualUsers) {
        assertThat(shift.getAssignments().stream().map(ScheduledShiftAssignment::getUser).collect(Collectors.toSet())).isEqualTo(Set.of(actualUsers));
    }
}
