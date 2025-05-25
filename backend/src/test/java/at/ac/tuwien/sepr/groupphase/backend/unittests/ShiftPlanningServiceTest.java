package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.entity.PlanBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftDayBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShiftWeekBlueprint;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ConcreteShiftPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.DepartmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PlanBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ScheduledShiftRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftDayBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShiftWeekBlueprintRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ShiftPlanningServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.TimeService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ShiftPlanningValidatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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


        shiftPlanningService = new ShiftPlanningServiceImpl(
            timeService,
            new ShiftPlanningValidatorImpl(),
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
        var day = new ShiftDayBlueprint.Builder()
            .withDay(DayOfWeek.MONDAY)
            .withStartTime(LocalTime.of(6, 0))
            .withDuration(Duration.ofHours(8))
            .build();

        var week = new ShiftWeekBlueprint.Builder()
            .withIndex(0)
            .withDays(List.of(day))
            .build();

        var shift = new ShiftBlueprint.Builder()
            .withDescription("Frühschicht")
            .withManPower(3)
            .addWeek(0, week)
            .build();
        shift.setId(100L);

        var planBuilder = new PlanBlueprint.Builder()
            .withDepartment(department)
            .withDescription("Testplan")
            .addShift(shift)
            .build();


        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(shiftBlueprintRepository.findAllById(List.of(100L))).thenReturn(List.of(shift));
        when(timeService.nextMondayInMonth(any())).thenReturn(monday);
        when(planBlueprintRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN

        var createPlanBlueprintDto = new PlanBlueprintCreationDto(
            "Testplan",
            List.of(new PlanBlueprintCreationDto.ShiftBlueprintCreationDto("Frühschicht", 3, List.of(
                    new PlanBlueprintCreationDto.ShiftWeekBlueprintCreationDto(List.of(
                        new PlanBlueprintCreationDto.ShiftDayBlueprintCreationDto(
                            Optional.of(DayOfWeek.MONDAY),
                            Optional.of(LocalTime.of(6, 0)),
                            Optional.of(Duration.ofHours(8))
                        )
                    ))
                )
                )
            ), departmentId);
        PlanBlueprintDto result = shiftPlanningService.createPlanBlueprint(createPlanBlueprintDto);

        // THEN
        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals("Produktion", result.department()),
            () -> assertEquals(1, result.shifts().size()),
            () -> {
                ShiftBlueprintDto shiftBlueprintDto = result.shifts().getFirst();
                assertAll("ShiftDto",
                    () -> assertEquals("Frühschicht", shiftBlueprintDto.description()),
                    () -> assertEquals(3, shiftBlueprintDto.manPower()),
                    () -> assertFalse(shiftBlueprintDto.shiftWeeks().isEmpty()),
                    () -> assertThat(shiftBlueprintDto.shiftWeeks().getFirst().shiftDays())
                        .hasSize(1)
                        .allSatisfy(dayDto -> {
                            assertEquals(DayOfWeek.MONDAY, dayDto.day());
                            assertEquals(LocalTime.of(6, 0), dayDto.startTime());
                            assertEquals(Duration.ofHours(8), dayDto.duration());
                        })
                );
            }
        );
    }


    @Test
    void createPlan_Blueprint_withMissingDepartment_shouldThrowNotFound() {
        var departmentId = 42L;
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());
        var createPlanBlueprintDto = new PlanBlueprintCreationDto("Testplan", List.of(), departmentId);
        assertThrows(NotFoundException.class, () -> shiftPlanningService.createPlanBlueprint(createPlanBlueprintDto));

    }

}
