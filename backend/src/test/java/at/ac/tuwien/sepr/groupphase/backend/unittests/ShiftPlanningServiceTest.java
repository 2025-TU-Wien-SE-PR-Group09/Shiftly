package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
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

    private ShiftDayRepository shiftDayRepository;
    private ShiftRepository shiftRepository;
    private ShiftWeekRepository shiftWeekRepository;
    private DepartmentRepository departmentRepository;
    private TimeService timeService;
    private PlanBlueprintRepository planBlueprintRepository;
    private ScheduledShiftRepository scheduledShiftRepository;
    private ConcreteShiftPlanRepository concreteShiftPlanRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        shiftDayRepository = mock(ShiftDayRepository.class);
        shiftRepository = mock(ShiftRepository.class);
        shiftWeekRepository = mock(ShiftWeekRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        timeService = mock(TimeService.class);
        planBlueprintRepository = mock(PlanBlueprintRepository.class);
        scheduledShiftRepository = mock(ScheduledShiftRepository.class);
        userRepository = mock(UserRepository.class);
        concreteShiftPlanRepository = mock(ConcreteShiftPlanRepository.class);


        //TODO
        shiftPlanningService = new ShiftPlanningServiceImpl(
            shiftDayRepository,
            shiftWeekRepository,
            shiftRepository,
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
    void createPlan_shouldReturnPlanBlueprintDto_withAllFieldsCorrect() {
        // GIVEN
        Long departmentId = 1L;
        Department department = new Department();
        department.setId(departmentId);
        department.setName("Produktion");

        LocalDate monday = LocalDate.of(2025, 6, 2);
        Shift shift = new Shift("Frühschicht", 3);
        shift.setId(100L);

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(shiftRepository.findAllById(List.of(100L))).thenReturn(List.of(shift));
        when(timeService.nextMonday()).thenReturn(monday);
        when(planBlueprintRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        PlanBlueprintDto result = shiftPlanningService.createPlan(departmentId, List.of(100L));

        // THEN
        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals("Produktion", result.department()),
            () -> assertEquals(1, result.shifts().size()),
            () -> {
                ShiftDto shiftDto = result.shifts().getFirst();
                assertAll("ShiftDto",
                    () -> assertEquals(100L, shiftDto.id()),
                    () -> assertEquals("Frühschicht", shiftDto.description()),
                    () -> assertEquals(3, shiftDto.manPower()),
                    () -> assertTrue(shiftDto.shiftWeeks().isEmpty())
                );
            }
        );
    }

    @Test
    void createShift_shouldReturnShiftDto_withCorrectFields() {
        // GIVEN
        CreateShiftDto input = new CreateShiftDto(1L, "Nachtschicht", 5);
        Shift savedShift = new Shift("Nachtschicht", 5);
        savedShift.setId(123L);

        when(shiftRepository.save(any())).thenReturn(savedShift);

        // WHEN
        ShiftDto result = shiftPlanningService.createShift(input);

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
    void addWeekToShift_shouldReturnShiftDto_withCorrectWeekAndDays() {
        // GIVEN
        Long shiftId = 77L;
        Shift shift = new Shift("Spätschicht", 4);
        shift.setId(shiftId);

        ShiftWeekDto weekDto = new ShiftWeekDto(List.of(
            new ShiftDayDto(DayOfWeek.TUESDAY, LocalTime.of(14, 0), Duration.ofHours(4)),
            new ShiftDayDto(DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), Duration.ofHours(4))
        ));

        ShiftWeek shiftWeek = new ShiftWeek(0, shift);
        List<ShiftDay> savedDays = List.of(
            new ShiftDay(DayOfWeek.TUESDAY, LocalTime.of(14, 0), Duration.ofHours(4), shiftWeek),
            new ShiftDay(DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), Duration.ofHours(4), shiftWeek)
        );

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(shiftWeekRepository.save(any())).thenReturn(shiftWeek);
        when(shiftDayRepository.saveAll(any())).thenReturn(savedDays);
        when(shiftRepository.save(any())).thenReturn(shift);

        // WHEN
        ShiftDto result = shiftPlanningService.addWeekToShift(shiftId, weekDto);

        // THEN
        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(shiftId, result.id()),
            () -> assertEquals("Spätschicht", result.description()),
            () -> assertEquals(4, result.manPower()),
            () -> assertEquals(1, result.shiftWeeks().size()),
            () -> {
                ShiftWeekDto returnedWeek = result.shiftWeeks().getFirst();
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
    void createShift_withZeroManPower_shouldThrowConflict() {
        CreateShiftDto input = new CreateShiftDto(1L, "Leerer Shift", 0);
        assertThrows(ConflictException.class, () -> shiftPlanningService.createShift(input));
    }

    @Test
    void createPlan_withMissingDepartment_shouldThrowNotFound() {
        when(departmentRepository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> shiftPlanningService.createPlan(42L, List.of()));
    }

    @Test
    void createPlan_withNoShiftsFound_shouldThrowNotFound() {
        Department d = new Department();
        d.setId(1L);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(d));
        when(shiftRepository.findAllById(List.of(99L))).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> shiftPlanningService.createPlan(1L, List.of(99L)));
    }

    @Test
    void addWeekToShift_withMissingShift_shouldThrowNotFound() {
        when(shiftRepository.findById(777L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () ->
            shiftPlanningService.addWeekToShift(777L, new ShiftWeekDto(List.of()))
        );
    }
}
