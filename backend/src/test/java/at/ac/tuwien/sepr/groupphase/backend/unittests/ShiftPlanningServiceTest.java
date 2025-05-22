package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.*;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ShiftPlanningServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.TimeService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ShiftWeekValidatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShiftPlanningServiceTest {

    private ShiftPlanningServiceImpl shiftPlanningService;

    private ShiftDayBlueprintRepository shiftDayBlueprintRepository;
    private ShiftBlueprintRepository shiftBlueprintRepository;
    private ShiftWeekBlueprintRepository shiftWeekBlueprintRepository;
    private DepartmentRepository departmentRepository;
    private TimeService timeService;
    private PlanBlueprintRepository planBlueprintRepository;
    private ScheduledShiftRepository scheduledShiftRepository;
    private ConcreteShiftPlanRepository concreteShiftPlanRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        shiftDayBlueprintRepository = mock(ShiftDayBlueprintRepository.class);
        shiftBlueprintRepository = mock(ShiftBlueprintRepository.class);
        shiftWeekBlueprintRepository = mock(ShiftWeekBlueprintRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        timeService = mock(TimeService.class);
        planBlueprintRepository = mock(PlanBlueprintRepository.class);
        scheduledShiftRepository = mock(ScheduledShiftRepository.class);
        userRepository = mock(UserRepository.class);
        concreteShiftPlanRepository = mock(ConcreteShiftPlanRepository.class);


        //TODO
        shiftPlanningService = new ShiftPlanningServiceImpl(
            shiftDayBlueprintRepository,
            shiftWeekBlueprintRepository,
            shiftBlueprintRepository,
            timeService,
            new ShiftWeekValidatorImpl(),
            planBlueprintRepository,
            departmentRepository,
            concreteShiftPlanRepository,
            scheduledShiftRepository,
            userRepository
        );
    }

    @Test
    void createPlan_shouldReturnPlanBlueprintBlueprintDto_withAllFieldsCorrect() {
        // GIVEN
        Long departmentId = 1L;
        Department department = new Department();
        department.setId(departmentId);
        department.setName("Produktion");

        LocalDate monday = LocalDate.of(2025, 6, 2);
        ShiftBlueprint shiftBlueprint = new ShiftBlueprint("Frühschicht", 3);
        shiftBlueprint.setId(100L);

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(shiftBlueprintRepository.findAllById(List.of(100L))).thenReturn(List.of(shiftBlueprint));
        when(timeService.nextMonday()).thenReturn(monday);
        when(planBlueprintRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        PlanBlueprintDto result = shiftPlanningService.createPlanBlueprint(departmentId, List.of(100L));

        // THEN
        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals("Produktion", result.department()),
            () -> assertEquals(1, result.shifts().size()),
            () -> {
                ShiftBlueprintDto shiftBlueprintDto = result.shifts().getFirst();
                assertAll("ShiftDto",
                    () -> assertEquals(100L, shiftBlueprintDto.id()),
                    () -> assertEquals("Frühschicht", shiftBlueprintDto.description()),
                    () -> assertEquals(3, shiftBlueprintDto.manPower()),
                    () -> assertTrue(shiftBlueprintDto.shiftWeeks().isEmpty())
                );
            }
        );
    }

    @Test
    void createShift_shouldReturnShiftBlueprintDto_withCorrectFields() {
        // GIVEN
        CreateShiftBlueprintDto input = new CreateShiftBlueprintDto(1L, "Nachtschicht", 5);
        ShiftBlueprint savedShiftBlueprint = new ShiftBlueprint("Nachtschicht", 5);
        savedShiftBlueprint.setId(123L);

        when(shiftBlueprintRepository.save(any())).thenReturn(savedShiftBlueprint);

        // WHEN
        ShiftBlueprintDto result = shiftPlanningService.createShiftBlueprint(input);

        // THEN
        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(123L, result.id()),
            () -> assertEquals("Nachtschicht", result.description()),
            () -> assertEquals(5, result.manPower()),
            () -> assertNotNull(result.shiftWeeks()),
            () -> assertTrue(result.shiftWeeks().isEmpty())
        );
    }

    @Test
    void addWeekToShift_shouldReturnShiftDto_withCorrectWeeksAndDays() {
        // GIVEN
        Long shiftId = 77L;
        ShiftBlueprint shiftBlueprint = new ShiftBlueprint("Spätschicht", 4);
        shiftBlueprint.setId(shiftId);

        ShiftWeekBlueprintDto weekDto = new ShiftWeekBlueprintDto(List.of(
            new ShiftDayDto(DayOfWeek.TUESDAY, LocalTime.of(14, 0), Duration.ofHours(4)),
            new ShiftDayDto(DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), Duration.ofHours(4))
        ));

        ShiftWeekBlueprint shiftWeekBlueprint = new ShiftWeekBlueprint(0, shiftBlueprint);
        List<ShiftDayBlueprint> savedDays = List.of(
            new ShiftDayBlueprint(DayOfWeek.TUESDAY, LocalTime.of(14, 0), Duration.ofHours(4), shiftWeekBlueprint),
            new ShiftDayBlueprint(DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), Duration.ofHours(4), shiftWeekBlueprint)
        );

        when(shiftBlueprintRepository.findById(shiftId)).thenReturn(Optional.of(shiftBlueprint));
        when(shiftWeekBlueprintRepository.save(any())).thenReturn(shiftWeekBlueprint);
        when(shiftDayBlueprintRepository.saveAll(any())).thenReturn(savedDays);
        when(shiftBlueprintRepository.save(any())).thenReturn(shiftBlueprint);

        // WHEN
        ShiftBlueprintDto result = shiftPlanningService.addWeeksToShift(shiftId, List.of(weekDto));

        // THEN
        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(shiftId, result.id()),
            () -> assertEquals("Spätschicht", result.description()),
            () -> assertEquals(4, result.manPower()),
            () -> assertEquals(1, result.shiftWeeks().size()),
            () -> {
                ShiftWeekBlueprintDto returnedWeek = result.shiftWeeks().getFirst();
                assertEquals(2, returnedWeek.shiftDays().size());
                assertAll("ShiftDayDto checks",
                    () -> assertEquals(DayOfWeek.TUESDAY, returnedWeek.shiftDays().getFirst().day()),
                    () -> assertEquals(LocalTime.of(14, 0), returnedWeek.shiftDays().getFirst().startTime()),
                    () -> assertEquals(Duration.ofHours(4), returnedWeek.shiftDays().getFirst().duration()),
                    () -> assertEquals(DayOfWeek.WEDNESDAY, returnedWeek.shiftDays().get(1).day())
                );
            }
        );
    }

    @Test
    void createShift_Blueprint_withZeroManPower_shouldThrowConflict() {
        CreateShiftBlueprintDto input = new CreateShiftBlueprintDto(1L, "Leerer Shift", 0);
        assertThrows(ConflictException.class, () -> shiftPlanningService.createShiftBlueprint(input));
    }

    @Test
    void createPlan_Blueprint_withMissingDepartment_shouldThrowNotFound() {
        when(departmentRepository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> shiftPlanningService.createPlanBlueprint(42L, List.of()));
    }

    @Test
    void createPlan_Blueprint_withNoShiftsFound_shouldThrowNotFound() {
        Department d = new Department();
        d.setId(1L);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(d));
        when(shiftBlueprintRepository.findAllById(List.of(99L))).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> shiftPlanningService.createPlanBlueprint(1L, List.of(99L)));
    }

    @Test
    void addWeeksToShift_withMissingShift_shouldThrowNotFound() {
        when(shiftBlueprintRepository.findById(777L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () ->
            shiftPlanningService.addWeeksToShift(777L,List.of( new ShiftWeekBlueprintDto(List.of())))
        );
    }
}
