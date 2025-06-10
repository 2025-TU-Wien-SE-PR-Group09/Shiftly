package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.shift.CreatePlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.shift.PlanBlueprintResponse;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.department.DepartmentShiftplanCalendarResponse;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.shift.AddShiftToPlanBlueprintDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.shift.GenerateConcretePlanDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShiftRestMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.ApplicationUserResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.department.DepartmentCreateRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.department.DepartmentDetailRestResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentEditDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.department.DepartmentEditRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.employee.EmployeeListItemResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.employee.EmployeeRestResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ConcreteShiftPlan;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.DepartmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.EmployeeService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShiftPlanningService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.shift.ConcretePlanGenerateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentNameDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.employee.EmployeeDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.employee.EmployeeListItemDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserEmailDto;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Department")
@RestController
@RequestMapping("/api/departments")
public class DepartmentEndpoint {

    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    private final ShiftPlanningService shiftPlanningService;
    private final UserService userService;

    public DepartmentEndpoint(DepartmentService departmentService,
                              ShiftPlanningService shiftPlanningService,
                              EmployeeService employeeService,
                              UserService userService) {
        this.departmentService = departmentService;
        this.shiftPlanningService = shiftPlanningService;
        this.employeeService = employeeService;
        this.userService = userService;
    }

    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all departments")
    @ApiResponse(responseCode = "200", description = "List of all departments")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DepartmentDetailRestResponseDto> getAllDepartments() {
        return departmentService.getAllDepartments().stream()
            .map(dept -> new DepartmentDetailRestResponseDto(
                dept.getId(),
                dept.getName(),
                dept.getSupervisorEmail()))
            .toList();
    }

    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all supervisors")
    @ApiResponse(responseCode = "200", description = "List of all supervisors")
    @GetMapping(value = "/api/departments/supervisors", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ApplicationUserResponseDto> getAllAvailableSupervisors() {
        List<ApplicationUserResponseDto> availableUsers = userService.getAllAvailableUsers();
        availableUsers.addAll(userService.getAllSupervisors(false));
        return availableUsers;
    }

    @RolesAllowed({"ADMIN", "SUPERVISOR"})
    @Operation(summary = "Get all available employees")
    @ApiResponse(responseCode = "200", description = "List of all employees that can be invited to department")
    @GetMapping(value = "/api/departments/employees", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ApplicationUserResponseDto> getAllAvailableEmployees() {
        return userService.getAllAvailableUsers();
    }

    //TODO; Fix all roles being allowed (needed because depID ist needed to load shiftPlan in calendar)
    @Transactional
    @RolesAllowed({"ADMIN", "SUPERVISOR", "EMPLOYEE"})
    @Operation(summary = "Get department by name")
    @ApiResponse(responseCode = "200", description = "Get department by name")
    @GetMapping(path = "/{departmentName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DepartmentDetailRestResponseDto getDepartmentByName(@PathVariable(name = "departmentName") String departmentName) {
        return departmentService.getDepartmentByName(departmentName).map(d ->
            new DepartmentDetailRestResponseDto(
                d.id(),x
                d.name(),
                departmentService.getSupervisorByDepartmentName(d.name())
                    .map(UserEmailDto::email)
                    .orElse("NONE")
            )
        ).orElseThrow(() -> new NotFoundException("Department not found!"));
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
    @Operation(summary = "Edit existing department")
    @ApiResponse(responseCode = "200", description = "Department edited")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> editDepartment(@RequestBody @Valid DepartmentEditRestDto restDto) throws NotFoundException {
        DepartmentEditDto serviceDto = new DepartmentEditDto(
            restDto.getOldName(),
            restDto.getNewName(),
            restDto.getSupervisorEmail());
        departmentService.editDepartment(serviceDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Transactional
    @RolesAllowed({"ADMIN", "SUPERVISOR"})
    @Operation(summary = "Create shift plan(Blueprint) for a department")
    @ApiResponse(responseCode = "201", description = "Shiftplan for the department")
    @PostMapping(path = "/{departmentName}/shiftplanBlueprint",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public PlanBlueprintResponse createShiftplanBlueprint(
        @PathVariable(name = "departmentName") String departmentName,
        @RequestBody @Valid CreatePlanBlueprintDto blueprintDto) {

        DepartmentDto department = departmentService.getDepartmentByName(departmentName)
            .orElseThrow(() -> new NotFoundException("Department not found!"));

        var mapped = ShiftRestMapper.mapFromRequest(department.id(), blueprintDto);
        return ShiftPlanningMapper.Plans.toResponse(shiftPlanningService.createPlanBlueprint(mapped));
    }

    @Transactional
    @RolesAllowed({"ADMIN", "SUPERVISOR"})
    @Operation(summary = "Add shift to existing plan(Blueprint) for a department")
    @ApiResponse(responseCode = "201", description = "Add shift to existing plan(Blueprint) for a department")
    @PostMapping(path = "/{departmentName}/shiftplanBlueprint/add",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public PlanBlueprintResponse addShiftToPlanBlueprint(
        @PathVariable(name = "departmentName") String departmentName,
        @RequestBody @Valid AddShiftToPlanBlueprintDto blueprintDto) {

        DepartmentDto department = departmentService.getDepartmentByName(departmentName)
            .orElseThrow(() -> new NotFoundException("Department not found!"));

        var mapped = ShiftRestMapper.mapFromRequest(blueprintDto);
        return ShiftPlanningMapper.Plans.toResponse(shiftPlanningService.addShiftToPlan(mapped));
    }


    @Transactional
    @RolesAllowed({"ADMIN", "SUPERVISOR"})
    @Operation(summary = "Get shift plan for a department")
    @ApiResponse(responseCode = "200", description = "Shiftplan for the department")
    @GetMapping(path = "/{departmentName}/shiftplanBlueprint", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PlanBlueprintResponse> getShiftplanBlueprints(
        @PathVariable(name = "departmentName") String departmentName) {

        DepartmentDto department = departmentService.getDepartmentByName(departmentName)
            .orElseThrow(() -> new NotFoundException("Department not found!"));

        return department.plans().stream().map(ShiftPlanningMapper.Plans::toResponse).toList();
    }

    @RolesAllowed({"ADMIN", "SUPERVISOR"})
    @Transactional
    @Operation(summary = "Generate concrete shift plan for the given department and return the scheduled shifts")
    @ApiResponse(responseCode = "201", description = "Concrete shift plan generated and returned")
    @PostMapping(path = "/{id}/generate-concrete-plan", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> generateConcretePlan(@RequestBody @Valid GenerateConcretePlanDto generateConcretePlanDto, @PathVariable("id") Long id) {
        ConcretePlanGenerateDto mapped = ShiftRestMapper.mapFromRequest(id, generateConcretePlanDto);

        ConcreteShiftPlan plan = shiftPlanningService.generateConcreteQuarterlyPlan(mapped);

        return ResponseEntity.status(HttpStatus.CREATED).build();
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

    @RolesAllowed({"ADMIN", "SUPERVISOR", "EMPLOYEE"})
    @Transactional
    @Operation(summary = "Get the concrete shift plan for the given department and return the scheduled shifts in suitable calendar format")
    @ApiResponse(responseCode = "201", description = "Concrete shift plan in calendar format.")
    @GetMapping(path = "/{departmentId}/shiftplan",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DepartmentShiftplanCalendarResponse> getConcreteShiftplan(@PathVariable("departmentId") Long id) {

        var plan = shiftPlanningService.getCurrentConcretePlan(id);
        List<DepartmentShiftplanCalendarResponse.ScheduledShift> result = new ArrayList<>();

        //TODO: move to service/mapper
        for (ScheduledShift shift : plan.getScheduledShifts()) {
            ShiftBlueprint blueprint = shift.getShift();

            for (ShiftWeekBlueprint week : blueprint.getShiftWeeks()) {
                for (ShiftDayBlueprint dayBlueprint : week.getDays()) {
                    LocalDateTime start = shift.getWeekStartDate()
                        .with(dayBlueprint.getDay())
                        .atTime(dayBlueprint.getStartTime());

                    LocalDateTime end = start.plus(dayBlueprint.getDuration());

                    List<String> workers = shift.getAssignments().stream()
                        .map(a -> a.getUser().getEmail())
                        .toList();

                    DepartmentShiftplanCalendarResponse.ScheduledShift shiftDto =
                        new DepartmentShiftplanCalendarResponse.ScheduledShift(
                            blueprint.getDescription(),
                            new DepartmentShiftplanCalendarResponse.Day(start, end),
                            workers
                        );

                    result.add(shiftDto);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new DepartmentShiftplanCalendarResponse(result));
    }

    @DeleteMapping(path = "/{departmentName}")
    @RolesAllowed({"ADMIN"})
    @Operation(summary = "Delete a department")
    @ApiResponse(responseCode = "200", description = "Department deleted successfully")
    public ResponseEntity<Void> deleteDepartment(@PathVariable("departmentName") String departmentName) {
        departmentService.deleteDepartmentByName(departmentName);
        return ResponseEntity.ok().build();
    }

}