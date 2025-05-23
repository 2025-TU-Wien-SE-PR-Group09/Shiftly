package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.createplanblueprint.CreatePlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentCreateRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentDetailRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.getplanblueprint.PlanBlueprintResponse;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ScheduledShiftResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.DepartmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ScheduledShiftDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDayDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.ShiftPlanningMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Department")
@RestController
@RequestMapping("/api/departments")
public class DepartmentEndpoint {

    private final DepartmentService departmentService;
    private final ShiftPlanningService shiftPlanningService;

    public DepartmentEndpoint(DepartmentService departmentService, ShiftPlanningService shiftPlanningService) {
        this.departmentService = departmentService;
        this.shiftPlanningService = shiftPlanningService;
    }

    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all departments")
    @ApiResponse(responseCode = "200", description = "List of all departments")
    @GetMapping
    public List<DepartmentDetailRestDto> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @Transactional
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Create a new department")
    @ApiResponse(responseCode = "201", description = "New department created")
    @PostMapping
    public ResponseEntity<Void> createDepartment(@RequestBody @Valid DepartmentCreateRestDto restDto) {
        DepartmentCreateDto serviceDto = new DepartmentCreateDto(
            restDto.getName(),
            restDto.getSupervisorEmail());
        departmentService.createDepartment(serviceDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Transactional
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Create shift plan(Blueprint) for a department")
    @ApiResponse(responseCode = "201", description = "Shiftplan for the department")
    @PostMapping("/{departmentName}/shiftplanBlueprint")
    public List<PlanBlueprintResponse> createShiftplanBlueprint(
        @PathVariable(name = "departmentName") String departmentName,
        @RequestBody @Valid CreatePlanBlueprintDto blueprintDto) {

        DepartmentDto department = departmentService.getDepartmentByName(departmentName)
            .orElseThrow(() -> new NotFoundException("Department not found!"));

        boolean hasPlan = !department.plans().isEmpty();


        //TODO: add all shifts to the plan
        var mappedShift = ShiftPlanningMapper.Shifts.toDto(department.id(), blueprintDto.getShifts().getFirst());
        var shift = shiftPlanningService.createShiftBlueprint(mappedShift);

        //TODO: add validation in service instead of filtering invalid shifts
        var mappedWeeks = blueprintDto.getShifts().getFirst().getShiftWeeks().stream().map(sw ->
            new ShiftWeekBlueprintDto(
                sw.getShiftDays()
                    .stream().map(ShiftPlanningMapper.ShiftDays::toDto)
                    .filter(x -> !(x.day().isEmpty() || x.duration().isEmpty() || x.startTime().isEmpty()))
                    .map(x -> new ShiftDayDto(
                            x.day().get(),
                            x.startTime().get(),
                            x.duration().get()
                        )
                    ).toList()
            )
        ).toList();

        shift = shiftPlanningService.addWeeksToShift(shift.id(), mappedWeeks);

        PlanBlueprintDto planBlueprintDto;
        if (hasPlan) {
            planBlueprintDto = shiftPlanningService.addShiftToCurrentPlan(department.id(), shift);
        } else {
            planBlueprintDto = shiftPlanningService.createPlanBlueprint(department.id(), List.of(shift.id()));
        }
        return List.of(ShiftPlanningMapper.Plans.toResponse(planBlueprintDto));
    }

    @Transactional
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get shift plan for a department")
    @ApiResponse(responseCode = "200", description = "Shiftplan for the department")
    @GetMapping("/{departmentName}/shiftplanBlueprint")
    public List<PlanBlueprintResponse> getShiftplanBlueprints(
        @PathVariable(name = "departmentName") String departmentName) {

        DepartmentDto department = departmentService.getDepartmentByName(departmentName)
            .orElseThrow(() -> new NotFoundException("Department not found!"));

        return department.plans().stream().map(ShiftPlanningMapper.Plans::toResponse).toList();
    }


    @Transactional
    @Operation(summary = "Generate concrete shift plan for the given department and return the scheduled shifts")
    @ApiResponse(responseCode = "201", description = "Concrete shift plan generated and returned")
    @PostMapping("/{id}/generate-concrete-plan")
    public ResponseEntity<List<ScheduledShiftResponseDto>> generateConcretePlan(@PathVariable("id") Long id) {
        ConcreteShiftPlan plan = shiftPlanningService.generateConcreteQuarterlyPlan(id);
        List<ScheduledShiftResponseDto> response = plan.getScheduledShifts().stream()
            .map(s -> new ScheduledShiftResponseDto(
                s.getId().getCalendarWeek(),
                s.getId().getCalendarYear(),
                s.getShift().getDescription(),
                s.getWeekStartDate()
            ))
            .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Transactional
    @Operation(summary = "Get detailed scheduled shifts for a department")
    @ApiResponse(responseCode = "200", description = "Detailed shifts returned")
    @GetMapping("/{departmentId}/concrete-plan-details")
    public List<ScheduledShiftDetailDto> getDetailedConcretePlan(
        @PathVariable("departmentId") Long departmentId
    ) {
        return shiftPlanningService.getCurrentConcretePlan(departmentId);
    }
}