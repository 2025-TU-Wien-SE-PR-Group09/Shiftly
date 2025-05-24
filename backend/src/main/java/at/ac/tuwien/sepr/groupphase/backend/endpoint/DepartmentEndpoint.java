package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentCreateRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DepartmentDetailRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeListItemResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeRestResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ScheduledShiftResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.createplanblueprint.CreatePlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.getplanblueprint.PlanBlueprintResponse;
import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.DepartmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.EmployeeService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.DepartmentNameDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.EmployeeDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.EmployeeListItemDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.ScheduledShiftDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.PlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftDayDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ShiftWeekBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.service.mapper.ShiftPlanningMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private final EmployeeService employeeService;

    private final ShiftPlanningService shiftPlanningService;

    public DepartmentEndpoint(DepartmentService departmentService,
                              ShiftPlanningService shiftPlanningService,
                              EmployeeService employeeService) {
        this.departmentService = departmentService;
        this.shiftPlanningService = shiftPlanningService;
        this.employeeService = employeeService;
    }

    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all departments")
    @ApiResponse(responseCode = "200", description = "List of all departments")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DepartmentDetailRestDto> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @Transactional
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Create a new department")
    @ApiResponse(responseCode = "201", description = "New department created")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
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
    @PostMapping(path = "/{departmentName}/shiftplanBlueprint",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
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
    @GetMapping(path = "/{departmentName}/shiftplanBlueprint", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PlanBlueprintResponse> getShiftplanBlueprints(
        @PathVariable(name = "departmentName") String departmentName) {

        DepartmentDto department = departmentService.getDepartmentByName(departmentName)
            .orElseThrow(() -> new NotFoundException("Department not found!"));

        return department.plans().stream().map(ShiftPlanningMapper.Plans::toResponse).toList();
    }

    @Transactional
    @RolesAllowed({"SUPERVISOR"})
    @Operation(summary = "Add an employee to a department")
    @ApiResponse(responseCode = "200", description = "Successfully added employee to department")
    @PostMapping(path = "/{departmentName}/addEmployee/{employeeEmail}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmployeeRestResponseDto addEmployeeToDepartment(
        @PathVariable(name = "departmentName") String departmentName,
        @PathVariable(name = "employeeEmail") String employeeEmail) {

        DepartmentDto department = departmentService.getDepartmentByName(departmentName)
            .orElseThrow(() -> new NotFoundException("Department not found!"));

        // TODO: Verify if user has access to this department

        EmployeeDto employee = new EmployeeDto(employeeEmail, department.id());
        employee = employeeService.convertUserToEmployee(employee);

        // in this case, a mapper function cannot be used because the service
        // does not require the department name, only the id
        // and the rest response does not require the department id, only the name
        return new EmployeeRestResponseDto(employee.email(), department.name());
    }

    @RolesAllowed({"SUPERVISOR"})
    @Operation(summary = "List all employees of a department")
    @ApiResponse(responseCode = "200", description = "List all employees of a department")
    @GetMapping(path = "/{departmentName}/employees", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public List<EmployeeListItemResponseDto> getEmployeesOfDepartment(
        @PathVariable(name = "departmentName") String departmentName) {

        DepartmentDto department = departmentService.getDepartmentByName(departmentName)
            .orElseThrow(() -> new NotFoundException("Department not found!"));

        // TODO: Verify if user has access to this department

        List<EmployeeListItemDto> employees = employeeService.getEmployeesOfDepartment(
            new DepartmentNameDto(department.name())
        );

        return employees.stream().map(EmployeeListItemResponseDto::from).toList();
    }


    @Transactional
    @Operation(summary = "Generate concrete shift plan for the given department and return the scheduled shifts")
    @ApiResponse(responseCode = "201", description = "Concrete shift plan generated and returned")
    @PostMapping(path = "/{id}/generate-concrete-plan",
        produces = MediaType.APPLICATION_JSON_VALUE)
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
    @GetMapping(path = "/{departmentId}/concrete-plan-details", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScheduledShiftDetailDto> getDetailedConcretePlan(
        @PathVariable("departmentId") Long departmentId
    ) {
        return shiftPlanningService.getCurrentConcretePlan(departmentId);
    }
}